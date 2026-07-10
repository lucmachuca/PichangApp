package com.PichangApp.service;

import com.PichangApp.client.MatchFeignClient;
import com.PichangApp.client.UsuarioFeignClient;
import com.PichangApp.config.RabbitMQConfig;
import com.PichangApp.dto.CrearSalaRequest;
import com.PichangApp.dto.EnviarMensajeRequest;
import com.PichangApp.dto.MatchSocialDTO;
import com.PichangApp.dto.MensajeChatResponse;
import com.PichangApp.dto.MensajeCreadoEvent;
import com.PichangApp.dto.SalaChatResponse;
import com.PichangApp.dto.UsuarioBasicoDTO;
import com.PichangApp.model.BloqueoUsuario;
import com.PichangApp.model.MensajeChat;
import com.PichangApp.model.SalaChat;
import com.PichangApp.model.enums.EstadoSala;
import com.PichangApp.model.enums.TipoMensaje;
import com.PichangApp.repository.BloqueoUsuarioRepository;
import com.PichangApp.repository.MensajeChatRepository;
import com.PichangApp.repository.SalaChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaChatService {

    private final SalaChatRepository salaChatRepository;
    private final MensajeChatRepository mensajeChatRepository;
    private final BloqueoUsuarioRepository bloqueoUsuarioRepository;
    private final UsuarioFeignClient usuarioFeignClient;
    private final MatchFeignClient matchFeignClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public SalaChatResponse crearSala(CrearSalaRequest request) {
        validarMatchParaSala(request.matchSocialId(), request.usuarioAId(), request.usuarioBId());

        salaChatRepository.findByMatchSocialId(request.matchSocialId())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "SalaChat already exists for SocialMatch: " + request.matchSocialId()
                    );
                });

        SalaChat sala = SalaChat.builder()
                .matchSocialId(request.matchSocialId())
                .usuarioAId(request.usuarioAId())
                .usuarioBId(request.usuarioBId())
                .estado(EstadoSala.ACTIVA)
                .build();

        sala = salaChatRepository.save(sala);

        log.info(
                "SalaChat {} created for SocialMatch {}",
                sala.getId(),
                request.matchSocialId()
        );

        Map<Long, UsuarioBasicoDTO> usuariosCache = new HashMap<>();
        return toResponse(sala, usuariosCache, Set.of());
    }

    @Transactional
    public MensajeChatResponse enviarMensaje(Long salaId, EnviarMensajeRequest request) {
        SalaChat sala = salaChatRepository.findById(salaId)
                .orElseThrow(() -> new IllegalArgumentException("SalaChat not found: " + salaId));

        if (sala.getEstado() != EstadoSala.ACTIVA) {
            throw new IllegalStateException("Cannot send mensajes to an archived sala.");
        }

        validarMatchParaSala(
                sala.getMatchSocialId(),
                sala.getUsuarioAId(),
                sala.getUsuarioBId()
        );

        if (!sala.getUsuarioAId().equals(request.remitenteId()) &&
                !sala.getUsuarioBId().equals(request.remitenteId())) {
            throw new IllegalArgumentException(
                    "User " + request.remitenteId() + " is not a participant in this sala."
            );
        }

        Long receptorId = obtenerReceptorId(
                sala.getUsuarioAId(),
                sala.getUsuarioBId(),
                request.remitenteId()
        );

        boolean remitenteBloqueoReceptor = bloqueoUsuarioRepository
                .existsByIdUsuarioOrigenAndIdUsuarioBloqueado(request.remitenteId(), receptorId);

        boolean receptorBloqueoRemitente = bloqueoUsuarioRepository
                .existsByIdUsuarioOrigenAndIdUsuarioBloqueado(receptorId, request.remitenteId());

        if (remitenteBloqueoReceptor || receptorBloqueoRemitente) {
            throw new IllegalStateException(
                    "No se puede enviar el mensaje porque existe un bloqueo entre los usuarios."
            );
        }

        MensajeChat mensaje = MensajeChat.builder()
                .salaChat(sala)
                .remitenteId(request.remitenteId())
                .contenido(request.contenido())
                .tipoMensaje(request.tipoMensaje() != null ? request.tipoMensaje() : TipoMensaje.TEXTO)
                .mediaUrl(request.mediaUrl())
                .build();

        mensaje = mensajeChatRepository.save(mensaje);

        MensajeChatResponse response = toMessageResponse(mensaje);

        MensajeCreadoEvent event = new MensajeCreadoEvent(
                mensaje.getId(),
                sala.getId(),
                sala.getMatchSocialId(),
                request.remitenteId(),
                receptorId,
                mensaje.getContenido(),
                mensaje.getFechaEnvio()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_COMUNICACION,
                RabbitMQConfig.ROUTING_KEY_MENSAJE_CREADO,
                event
        );

        log.info(
                "MensajeCreadoEvent enviado a RabbitMQ. mensajeId={}, salaId={}, remitente={}, destinatario={}",
                event.mensajeId(),
                event.salaId(),
                event.remitenteId(),
                event.destinatarioId()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<SalaChatResponse> obtenerSalasPorUsuario(Long userId) {
        List<SalaChat> salas = salaChatRepository.findByUserIdAndStatus(userId, EstadoSala.ACTIVA);

        Map<Long, UsuarioBasicoDTO> usuariosCache = new HashMap<>();

        List<Long> otrosUsuariosIds = salas.stream()
                .map(sala -> obtenerReceptorId(sala.getUsuarioAId(), sala.getUsuarioBId(), userId))
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        Set<Long> salasBloqueadasIds = obtenerSalasBloqueadasIds(userId, otrosUsuariosIds, salas);

        return salas.stream()
                .map(sala -> toResponse(sala, usuariosCache, salasBloqueadasIds))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MensajeChatResponse> obtenerMensajes(Long salaId, int page, int size) {
        int sizeSeguro = Math.max(1, Math.min(size, 80));

        return mensajeChatRepository
                .findBySalaChatIdOrderByFechaEnvioDesc(salaId, PageRequest.of(page, sizeSeguro))
                .map(this::toMessageResponse);
    }

    private Set<Long> obtenerSalasBloqueadasIds(
            Long userId,
            List<Long> otrosUsuariosIds,
            List<SalaChat> salas
    ) {
        if (otrosUsuariosIds == null || otrosUsuariosIds.isEmpty()) {
            return Set.of();
        }

        List<BloqueoUsuario> bloqueos = bloqueoUsuarioRepository.findBloqueosEntreUsuarioYOtros(
                userId,
                otrosUsuariosIds
        );

        if (bloqueos.isEmpty()) {
            return Set.of();
        }

        Set<Long> usuariosBloqueadosEntreSi = new HashSet<>();

        for (BloqueoUsuario bloqueo : bloqueos) {
            if (userId.equals(bloqueo.getIdUsuarioOrigen())) {
                usuariosBloqueadosEntreSi.add(bloqueo.getIdUsuarioBloqueado());
            }

            if (userId.equals(bloqueo.getIdUsuarioBloqueado())) {
                usuariosBloqueadosEntreSi.add(bloqueo.getIdUsuarioOrigen());
            }
        }

        Set<Long> salasBloqueadasIds = new HashSet<>();

        for (SalaChat sala : salas) {
            Long otroUsuarioId = obtenerReceptorId(sala.getUsuarioAId(), sala.getUsuarioBId(), userId);

            if (usuariosBloqueadosEntreSi.contains(otroUsuarioId)) {
                salasBloqueadasIds.add(sala.getId());
            }
        }

        return salasBloqueadasIds;
    }

    private void validarMatchParaSala(Long matchSocialId, Long usuarioAId, Long usuarioBId) {
        MatchSocialDTO match;

        try {
            match = matchFeignClient.obtenerMatchPorId(matchSocialId);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo validar el MatchSocial " + matchSocialId + " contra msvc-match."
            );
        }

        if (match == null || match.id() == null) {
            throw new IllegalStateException("El MatchSocial no existe.");
        }

        if (match.activo() == null || !match.activo()) {
            throw new IllegalStateException("El MatchSocial no está activo.");
        }

        boolean mismosUsuariosDirecto =
                match.usuarioAId().equals(usuarioAId) &&
                        match.usuarioBId().equals(usuarioBId);

        boolean mismosUsuariosInvertido =
                match.usuarioAId().equals(usuarioBId) &&
                        match.usuarioBId().equals(usuarioAId);

        if (!mismosUsuariosDirecto && !mismosUsuariosInvertido) {
            throw new IllegalStateException(
                    "La sala no corresponde a los usuarios del MatchSocial."
            );
        }
    }

    private Long obtenerReceptorId(Long usuarioAId, Long usuarioBId, Long remitenteId) {
        if (usuarioAId.equals(remitenteId)) {
            return usuarioBId;
        }

        if (usuarioBId.equals(remitenteId)) {
            return usuarioAId;
        }

        return 0L;
    }

    private SalaChatResponse toResponse(
            SalaChat sala,
            Map<Long, UsuarioBasicoDTO> usuariosCache,
            Set<Long> salasBloqueadasIds
    ) {
        UsuarioBasicoDTO usuarioA = obtenerUsuarioSeguro(sala.getUsuarioAId(), usuariosCache);
        UsuarioBasicoDTO usuarioB = obtenerUsuarioSeguro(sala.getUsuarioBId(), usuariosCache);

        MensajeChatResponse ultimoMensaje = mensajeChatRepository
                .findFirstBySalaChatIdOrderByFechaEnvioDesc(sala.getId())
                .map(this::toMessageResponse)
                .orElse(null);

        boolean bloqueada = salasBloqueadasIds != null && salasBloqueadasIds.contains(sala.getId());

        return new SalaChatResponse(
                sala.getId(),
                sala.getMatchSocialId(),
                sala.getUsuarioAId(),
                sala.getUsuarioBId(),
                usuarioA.nombreCompleto(),
                usuarioB.nombreCompleto(),
                usuarioA.username(),
                usuarioB.username(),
                usuarioA.fotoUrl(),
                usuarioB.fotoUrl(),
                sala.getEstado(),
                sala.getFechaCreacion(),
                ultimoMensaje,
                bloqueada
        );
    }

    private MensajeChatResponse toMessageResponse(MensajeChat msg) {
        return new MensajeChatResponse(
                msg.getId(),
                msg.getSalaChat().getId(),
                msg.getRemitenteId(),
                null,
                null,
                msg.getContenido(),
                msg.getTipoMensaje(),
                msg.getMediaUrl(),
                msg.getFechaEnvio()
        );
    }

    private UsuarioBasicoDTO obtenerUsuarioSeguro(
            Long id,
            Map<Long, UsuarioBasicoDTO> usuariosCache
    ) {
        if (usuariosCache != null && usuariosCache.containsKey(id)) {
            return usuariosCache.get(id);
        }

        UsuarioBasicoDTO usuario;

        try {
            usuario = usuarioFeignClient.obtenerUsuarioBasico(id);
        } catch (Exception e) {
            log.warn(
                    "No se pudo obtener usuario {} desde msvc-usuario. Se usará fallback. Motivo: {}",
                    id,
                    e.getMessage()
            );

            usuario = new UsuarioBasicoDTO(
                    id,
                    "usuario" + id,
                    "Usuario",
                    String.valueOf(id),
                    null,
                    null
            );
        }

        if (usuariosCache != null) {
            usuariosCache.put(id, usuario);
        }

        return usuario;
    }
}
