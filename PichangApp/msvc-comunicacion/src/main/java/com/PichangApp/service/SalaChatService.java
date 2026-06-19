package com.PichangApp.service;

import com.PichangApp.client.MatchFeignClient;
import com.PichangApp.client.UsuarioFeignClient;
import com.PichangApp.dto.CrearSalaRequest;
import com.PichangApp.dto.EnviarMensajeRequest;
import com.PichangApp.dto.MatchSocialDTO;
import com.PichangApp.dto.MensajeChatResponse;
import com.PichangApp.dto.SalaChatResponse;
import com.PichangApp.dto.UsuarioBasicoDTO;
import com.PichangApp.model.MensajeChat;
import com.PichangApp.model.SalaChat;
import com.PichangApp.model.enums.EstadoSala;
import com.PichangApp.model.enums.TipoMensaje;
import com.PichangApp.repository.BloqueoUsuarioRepository;
import com.PichangApp.repository.MensajeChatRepository;
import com.PichangApp.repository.SalaChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaChatService {

    private final SalaChatRepository salaChatRepository;
    private final MensajeChatRepository mensajeChatRepository;
    private final BloqueoUsuarioRepository bloqueoUsuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UsuarioFeignClient usuarioFeignClient;
    private final MatchFeignClient matchFeignClient;

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

        return toResponse(sala);
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

        messagingTemplate.convertAndSend("/topic/sala/" + salaId, response);

        return response;
    }

    public List<SalaChatResponse> obtenerSalasPorUsuario(Long userId) {
        return salaChatRepository.findByUserIdAndStatus(userId, EstadoSala.ACTIVA)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<MensajeChatResponse> obtenerMensajes(Long salaId, int page, int size) {
        return mensajeChatRepository.findBySalaChatIdOrderByFechaEnvioDesc(salaId, PageRequest.of(page, size))
                .map(this::toMessageResponse);
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

        throw new IllegalArgumentException("El remitente no pertenece a la sala.");
    }

    private SalaChatResponse toResponse(SalaChat sala) {
        UsuarioBasicoDTO usuarioA = obtenerUsuarioSeguro(sala.getUsuarioAId());
        UsuarioBasicoDTO usuarioB = obtenerUsuarioSeguro(sala.getUsuarioBId());

        return new SalaChatResponse(
                sala.getId(),
                sala.getMatchSocialId(),
                sala.getUsuarioAId(),
                sala.getUsuarioBId(),
                usuarioA.nombreCompleto(),
                usuarioB.nombreCompleto(),
                usuarioA.username(),
                usuarioB.username(),
                sala.getEstado(),
                sala.getFechaCreacion()
        );
    }

    private MensajeChatResponse toMessageResponse(MensajeChat msg) {
        UsuarioBasicoDTO remitente = obtenerUsuarioSeguro(msg.getRemitenteId());

        return new MensajeChatResponse(
                msg.getId(),
                msg.getSalaChat().getId(),
                msg.getRemitenteId(),
                remitente.nombreCompleto(),
                remitente.username(),
                msg.getContenido(),
                msg.getTipoMensaje(),
                msg.getMediaUrl(),
                msg.getFechaEnvio()
        );
    }

    private UsuarioBasicoDTO obtenerUsuarioSeguro(Long id) {
        try {
            return usuarioFeignClient.obtenerUsuarioBasico(id);
        } catch (Exception e) {
            log.warn(
                    "No se pudo obtener usuario {} desde msvc-usuario. Se usará fallback. Motivo: {}",
                    id,
                    e.getMessage()
            );

            return new UsuarioBasicoDTO(
                    id,
                    "usuario" + id,
                    "Usuario",
                    String.valueOf(id),
                    null
            );
        }
    }
}