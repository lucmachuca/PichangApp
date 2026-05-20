package com.PichangApp.msvc_seguridad.dtos;

import jakarta.validation.constraints.NotNull;

public record BloqueoRequest(
        @NotNull Long idUsuarioOrigen,
        @NotNull Long idUsuarioBloqueado
) {
}