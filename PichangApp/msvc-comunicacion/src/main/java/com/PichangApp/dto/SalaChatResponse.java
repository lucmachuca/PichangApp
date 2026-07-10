package com.PichangApp.dto;

import com.PichangApp.model.enums.EstadoSala;

import java.time.LocalDateTime;

public record SalaChatResponse(
        Long id,
        Long matchSocialId,
        Long usuarioAId,
        Long usuarioBId,
        String usuarioANombre,
        String usuarioBNombre,
        String usuarioAUsername,
        String usuarioBUsername,
        String usuarioAFotoUrl,
        String usuarioBFotoUrl,
        EstadoSala estado,
        LocalDateTime fechaCreacion,
        MensajeChatResponse ultimoMensaje,
        Boolean bloqueada
) {
}
