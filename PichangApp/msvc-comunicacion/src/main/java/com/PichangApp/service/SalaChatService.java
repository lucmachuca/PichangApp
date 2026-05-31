package com.PichangApp.service;

import com.PichangApp.dto.CrearSalaRequest;
import com.PichangApp.dto.EnviarMensajeRequest;
import com.PichangApp.dto.MensajeChatResponse;
import com.PichangApp.dto.SalaChatResponse;
import com.PichangApp.model.MensajeChat;
import com.PichangApp.model.SalaChat;
import com.PichangApp.model.enums.EstadoSala;
import com.PichangApp.repository.BloqueoUsuarioRepository;
import com.PichangApp.repository.MensajeChatRepository;
import com.PichangApp.repository.SalaChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    /**
     * Crea una SalaChat a partir de un SocialMatch.
     * Solo se permite una sala por SocialMatch.
     */
    @Transactional
    public SalaChatResponse crearSala(CrearSalaRequest request) {
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
        log.info("SalaChat {} created for SocialMatch {}", sala.getId(), request.matchSocialId());

        return toResponse(sala);
    }

    /**
     * Envía un mensaje en una sala.
     * Valida:
     * 1. Que la sala exista.
     * 2. Que la sala esté activa.
     * 3. Que el remitente pertenezca a la sala.
     * 4. Que no exista bloqueo entre los participantes.
     */
    @Transactional
    public MensajeChatResponse enviarMensaje(Long salaId, EnviarMensajeRequest request) {
        SalaChat sala = salaChatRepository.findById(salaId)
                .orElseThrow(() -> new IllegalArgumentException("SalaChat not found: " + salaId));

        if (sala.getEstado() != EstadoSala.ACTIVA) {
            throw new IllegalStateException("Cannot send mensajes to an archived sala.");
        }

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
                .build();

        mensaje = mensajeChatRepository.save(mensaje);
        return toMessageResponse(mensaje);
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
        return new SalaChatResponse(
                sala.getId(),
                sala.getMatchSocialId(),
                sala.getUsuarioAId(),
                sala.getUsuarioBId(),
                sala.getEstado(),
                sala.getFechaCreacion()
        );
    }

    private MensajeChatResponse toMessageResponse(MensajeChat msg) {
        return new MensajeChatResponse(
                msg.getId(),
                msg.getSalaChat().getId(),
                msg.getRemitenteId(),
                msg.getContenido(),
                msg.getFechaEnvio()
        );
    }
}