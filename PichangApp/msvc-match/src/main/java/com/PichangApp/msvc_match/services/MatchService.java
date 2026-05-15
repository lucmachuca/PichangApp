package com.PichangApp.msvc_match.services;

import com.PichangApp.msvc_match.dtos.MatchSocialResponse;
import com.PichangApp.msvc_match.dtos.InteraccionRequest;
import com.PichangApp.msvc_match.dtos.InteraccionResponse;
import com.PichangApp.msvc_match.models.MatchSocial;
import com.PichangApp.msvc_match.models.Interaccion;
import com.PichangApp.msvc_match.models.enums.TipoInteraccion;
import com.PichangApp.msvc_match.repositories.MatchSocialRepository;
import com.PichangApp.msvc_match.repositories.InteraccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final InteraccionRepository interaccionRepository;
    private final MatchSocialRepository matchSocialRepository;

    /**
     * Registra la acciÃ³n de un usuario (como dar "Me Gusta" o rechazar) hacia otro usuario.
     * Flujo principal:
     * 1. El Usuario A le da "Me Gusta" al Usuario B.
     * 2. El sistema revisa si el Usuario B ya le habÃ­a dado "Me Gusta" al Usuario A antes.
     * 3. Si ambos se dieron "Me Gusta", Â¡tenemos un Match (conexiÃ³n exitosa)!
     */
    @Transactional
    public InteraccionResponse registerInteraccion(InteraccionRequest request) {
        // Regla de negocio: Un usuario no puede interactuar consigo mismo.
        if (request.usuarioOrigenId().equals(request.usuarioDestinoId())) {
            throw new IllegalArgumentException("Un usuario no puede interactuar consigo mismo.");
        }

        // Regla de negocio: Un usuario no puede interactuar mÃ¡s de una vez con la misma persona.
        if (interaccionRepository.existsByUsuarioOrigenIdAndUsuarioDestinoId(request.usuarioOrigenId(), request.usuarioDestinoId())) {
            throw new IllegalStateException("El usuario ya ha interactuado con esta persona.");
        }

        // Creamos y guardamos la interacciÃ³n en la base de datos (PostgreSQL).
        Interaccion interaccion = Interaccion.builder()
                .usuarioOrigenId(request.usuarioOrigenId())
                .usuarioDestinoId(request.usuarioDestinoId())
                .tipo(request.tipo())
                .build();

        interaccion = interaccionRepository.save(interaccion);

        boolean hayMatch = false;

        // Si la interacciÃ³n es un "Me Gusta", verificamos si la otra persona tambiÃ©n nos dio "Me Gusta".
        if (request.tipo() == TipoInteraccion.ME_GUSTA) {
            hayMatch = checkAndCreateMatch(request.usuarioOrigenId(), request.usuarioDestinoId());
        }

        // Devolvemos el resultado de la interacciÃ³n, indicando si hubo match o no.
        return new InteraccionResponse(
                interaccion.getId(),
                interaccion.getUsuarioOrigenId(),
                interaccion.getUsuarioDestinoId(),
                interaccion.getTipo(),
                hayMatch,
                interaccion.getFechaCreacion()
        );
    }

    /**
     * Verifica si la persona que recibiÃ³ el "Me Gusta" ya nos habÃ­a dado un "Me Gusta" previamente.
     * Si es asÃ­, crea la conexiÃ³n oficial (MatchSocial).
     */
    private boolean checkAndCreateMatch(Long usuarioOrigenId, Long usuarioDestinoId) {
        // Buscamos si existe un "Me Gusta" recÃ­proco (del Destino hacia el Origen).
        var reciprocal = interaccionRepository.findByUsuarioOrigenIdAndUsuarioDestinoIdAndTipo(
                usuarioDestinoId, usuarioOrigenId, TipoInteraccion.ME_GUSTA
        );

        if (reciprocal.isPresent()) {
            // Verificamos que no exista ya un Match entre estas dos personas para no duplicarlo.
            var existing = matchSocialRepository.findByUsers(usuarioOrigenId, usuarioDestinoId);
            if (existing.isEmpty()) {
                // Ordenamos los IDs de menor a mayor para evitar duplicados invertidos en la base de datos
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
                log.info("Â¡Match creado exitosamente entre el Usuario {} y el Usuario {}!", userA, userB);

                // PRÃ“XIMO PASO (TODO): Avisar al sistema de chats para que les abra una conversaciÃ³n.
                // Esto se harÃ¡ de forma asÃ­ncrona para no retrasar la respuesta al usuario.
                return true;
            }
        }
        // Si no hay "Me Gusta" recÃ­proco, no hay Match aÃºn.
        return false;
    }

    /**
     * Obtiene la lista de todas las conexiones (Matches) activas de un usuario en particular.
     */
    public List<MatchSocialResponse> obtenerMatchesPorUsuario(Long userId) {
        return matchSocialRepository.findActiveMatchesByUserId(userId)
                .stream()
                .map(m -> new MatchSocialResponse(
                        m.getId(), m.getUsuarioAId(), m.getUsuarioBId(),
                        m.getActivo(), m.getFechaCreacion()
                ))
                .toList();
    }

    /**
     * Busca los detalles de un Match especÃ­fico usando su nÃºmero identificador (ID).
     */
    public MatchSocialResponse obtenerMatchPorId(Long matchId) {
        var match = matchSocialRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontrÃ³ el Match con el ID: " + matchId));
        return new MatchSocialResponse(
                match.getId(), match.getUsuarioAId(), match.getUsuarioBId(),
                match.getActivo(), match.getFechaCreacion()
        );
    }
}
