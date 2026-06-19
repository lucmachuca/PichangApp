package com.PichangApp.dto;

import java.time.LocalDateTime;

public record MatchSocialResponse(
        Long id,
        Long usuarioAId,
        Long usuarioBId,
        Boolean activo,
        LocalDateTime fechaCreacion
) {
}