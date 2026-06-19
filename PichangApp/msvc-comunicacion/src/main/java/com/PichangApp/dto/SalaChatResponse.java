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
        EstadoSala estado,
        LocalDateTime fechaCreacion
) {
}
