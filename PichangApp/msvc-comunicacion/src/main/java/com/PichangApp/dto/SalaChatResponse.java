package com.PichangApp.dto;

import java.time.LocalDateTime;

import com.PichangApp.model.enums.EstadoSala;

public record SalaChatResponse(
        Long id,
        Long matchSocialId,
        Long usuarioAId,
        Long usuarioBId,
        EstadoSala estado,
        LocalDateTime fechaCreacion
) {}
