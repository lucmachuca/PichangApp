package com.PichangApp.dto;

import java.time.LocalDateTime;

public record MatchSocialResponse(
                Long id,
                Long usuarioAId,
                Long usuarioBId,
                boolean activo,
                LocalDateTime fechaCreacion) {
}
