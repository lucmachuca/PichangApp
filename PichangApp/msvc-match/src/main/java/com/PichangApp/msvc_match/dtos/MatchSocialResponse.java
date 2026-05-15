package com.PichangApp.msvc_match.dtos;

import java.time.LocalDateTime;

public record MatchSocialResponse(
        Long id,
        Long usuarioAId,
        Long usuarioBId,
        boolean activo,
        LocalDateTime fechaCreacion
) {}
