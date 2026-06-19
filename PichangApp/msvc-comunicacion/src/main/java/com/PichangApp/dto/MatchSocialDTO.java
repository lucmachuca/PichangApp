package com.PichangApp.dto;

import java.time.LocalDateTime;

public record MatchSocialDTO(
        Long id,
        Long usuarioAId,
        Long usuarioBId,
        Boolean activo,
        LocalDateTime fechaCreacion
) {
}