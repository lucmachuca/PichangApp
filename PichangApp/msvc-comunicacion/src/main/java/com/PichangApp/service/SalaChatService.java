package com.PichangApp.service;

import com.PichangApp.dto.*;
import com.PichangApp.model.MensajeChat;
import com.PichangApp.model.SalaChat;
import com.PichangApp.model.enums.EstadoSala;
import org.springframework.stereotype.Service;

import com.PichangApp.repository.MensajeChatRepository;
import com.PichangApp.repository.SalaChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaChatService {

    private final SalaChatRepository salaChatRepository;
    private final MensajeChatRepository mensajeChatRepository;

    /**
     * Crea una SalaChat a partir de un SocialMatch.
     * Solo se permite una sala por SocialMatch (se rechazan duplicados).
     */
    @Transactional
    public SalaChatResponse crearSala(CrearSalaRequest request) {
        salaChatRepository.findBySocialMatchId(request.matchSocialId())
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
     * Valida que el remitente sea participante y que la sala esté ACTIVA.
     */
    @Transactional
    public MensajeChatResponse enviarMensaje(Long salaId, EnviarMensajeRequest request) {
        SalaChat sala = salaChatRepository.findById(salaId)
                .orElseThrow(() -> new IllegalArgumentException("SalaChat not found: " + salaId));

        if (sala.getEstado() != EstadoSala.ACTIVA) {
            throw new IllegalStateException("Cannot send mensajes to an archived sala.");
        }

        if (!sala.getUsuarioAId().equals(request.remitenteId()) && !sala.getUsuarioBId().equals(request.remitenteId())) {
            throw new IllegalArgumentException(
                    "User " + request.remitenteId() + " is not a participant in this sala."
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

    private SalaChatResponse toResponse(SalaChat sala) {
        return new SalaChatResponse(
                sala.getId(), sala.getMatchSocialId(),
                sala.getUsuarioAId(), sala.getUsuarioBId(),
                sala.getEstado(), sala.getFechaCreacion()
        );
    }

    private MensajeChatResponse toMessageResponse(MensajeChat msg) {
        return new MensajeChatResponse(
                msg.getId(), msg.getSalaChat().getId(),
                msg.getRemitenteId(), msg.getContenido(), msg.getFechaEnvio()
        );
    }
}
