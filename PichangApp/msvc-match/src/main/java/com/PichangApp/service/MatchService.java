package com.PichangApp.service;

import com.PichangApp.dto.MatchSocialResponse;
import com.PichangApp.dto.InteraccionRequest;
import com.PichangApp.dto.InteraccionResponse;
import com.PichangApp.model.MatchSocial;
import com.PichangApp.model.Interaccion;
import com.PichangApp.model.enums.TipoInteraccion;
import com.PichangApp.repository.MatchSocialRepository;
import com.PichangApp.repository.InteraccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.amqp.AmqpException;
import com.PichangApp.dto.MatchCreatedEvent;


import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final InteraccionRepository interaccionRepository;
    private final MatchSocialRepository matchSocialRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Registra la acción de un usuario (como dar "Me Gusta" o rechazar) hacia otro
     * usuario.
     * Flujo principal:
     * 1. El Usuario A le da "Me Gusta" al Usuario B.
     * 2. El sistema revisa si el Usuario B ya le había dado "Me Gusta" al Usuario A
     * antes.
     * 3. Si ambos se dieron "Me Gusta", ¡tenemos un Match (conexión exitosa)!
     */
    @Transactional
    public InteraccionResponse registerInteraccion(InteraccionRequest request) {
        // Regla de negocio: Un usuario no puede interactuar consigo mismo.
        if (request.usuarioOrigenId().equals(request.usuarioDestinoId())) {
            throw new IllegalArgumentException("Un usuario no puede interactuar consigo mismo.");
        }

        // Regla de negocio: Un usuario no puede interactuar más de una vez con la misma
        // persona.
        if (interaccionRepository.existsByUsuarioOrigenIdAndUsuarioDestinoId(request.usuarioOrigenId(),
                request.usuarioDestinoId())) {
            throw new IllegalStateException("El usuario ya ha interactuado con esta persona.");
        }

        // Creamos y guardamos la interacción en la base de datos (PostgreSQL).
        Interaccion interaccion = Interaccion.builder()
                .usuarioOrigenId(request.usuarioOrigenId())
                .usuarioDestinoId(request.usuarioDestinoId())
                .tipo(request.tipo())
                .build();

        interaccion = interaccionRepository.save(interaccion);

        boolean hayMatch = false;

        // Si la interacción es un "Me Gusta", verificamos si la otra persona también
        // nos dio "Me Gusta".
        if (request.tipo() == TipoInteraccion.ME_GUSTA) {
            hayMatch = checkAndCreateMatch(request.usuarioOrigenId(), request.usuarioDestinoId());
        }

        // Devolvemos el resultado de la interacción, indicando si hubo match o no.
        return new InteraccionResponse(
                interaccion.getId(),
                interaccion.getUsuarioOrigenId(),
                interaccion.getUsuarioDestinoId(),
                interaccion.getTipo(),
                hayMatch,
                interaccion.getFechaCreacion());
    }

    /**
     * Verifica si la persona que recibió el "Me Gusta" ya nos había dado un "Me
     * Gusta" previamente.
     * Si es así, crea la conexión oficial (MatchSocial).
     */
    private boolean checkAndCreateMatch(Long usuarioOrigenId, Long usuarioDestinoId) {
        // Buscamos si existe un "Me Gusta" recíproco (del Destino hacia el Origen).
        var reciprocal = interaccionRepository.findByUsuarioOrigenIdAndUsuarioDestinoIdAndTipo(
                usuarioDestinoId, usuarioOrigenId, TipoInteraccion.ME_GUSTA);

        if (reciprocal.isPresent()) {
            // Verificamos que no exista ya un Match entre estas dos personas para no
            // duplicarlo.
            var existing = matchSocialRepository.findByUsers(usuarioOrigenId, usuarioDestinoId);
            if (existing.isEmpty()) {
                // Ordenamos los IDs de menor a mayor para evitar duplicados invertidos en la
                // base de datos
                // (ej. Match 1-2 es lo mismo que Match 2-1).
                Long userA = Math.min(usuarioOrigenId, usuarioDestinoId);
                Long userB = Math.max(usuarioOrigenId, usuarioDestinoId);

                // Creamos el Match y lo guardamos como activo.
                MatchSocial match = MatchSocial.builder()
                        .usuarioAId(userA)
                        .usuarioBId(userB)
                        .activo(true)
                        .build();

                matchSocialRepository.save(match);
                log.info("¡Match creado exitosamente entre el Usuario {} y el Usuario {}!", userA, userB);

                MatchCreatedEvent event = new MatchCreatedEvent(match.getId(), userA, userB);
                eventPublisher.publishEvent(event);
                log.info("MatchCreatedEvent registrado localmente para envío diferido (matchId: {})", match.getId());

                return true;
            }
        }
        // Si no hay "Me Gusta" recíproco, no hay Match aún.
        return false;
    }

    /**
     * Obtiene la lista de todas las conexiones (Matches) activas de un usuario en
     * particular.
     */
    public List<MatchSocialResponse> obtenerMatchesPorUsuario(Long userId) {
        return matchSocialRepository.findActiveMatchesByUserId(userId)
                .stream()
                .map(m -> new MatchSocialResponse(
                        m.getId(), m.getUsuarioAId(), m.getUsuarioBId(),
                        m.getActivo(), m.getFechaCreacion()))
                .toList();
    }

    /**
     * Busca los detalles de un Match específico usando su número identificador
     * (ID).
     */
    public MatchSocialResponse obtenerMatchPorId(Long matchId) {
        var match = matchSocialRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el Match con el ID: " + matchId));
        return new MatchSocialResponse(
                match.getId(), match.getUsuarioAId(), match.getUsuarioBId(),
                match.getActivo(), match.getFechaCreacion());
    }

    /**
     * Este listener se dispara de forma automática ÚNICAMENTE DESPUÉS de que la 
     * transacción de base de datos se haya commiteado (guardado) con éxito.
     * Así evitamos que un fallo en RabbitMQ provoque un Rollback en PostgreSQL.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchCreated(MatchCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend("match.exchange", "match.created", event);
            log.info("MatchCreatedEvent enviado con éxito a RabbitMQ para matchId: {}", event.matchId());
        } catch (AmqpException e) {
            log.error("Error al enviar evento a RabbitMQ para matchId: {}. Motivo: {}", event.matchId(), e.getMessage());
            // En un sistema avanzado, aquí podríamos guardar el evento en una tabla "outbox" para reintentar luego.
        }
    }

    public List<Long> obtenerUsuariosInteractuados(Long usuarioOrigenId) {
        return interaccionRepository.findByUsuarioOrigenId(usuarioOrigenId)
                .stream()
                .map(Interaccion::getUsuarioDestinoId)
                .distinct()
                .toList();
    }
}
