package com.PichangApp.msvc_seguridad.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReporteRequest(
        @NotNull Long idUsuarioDenunciante,
        @NotNull Long idUsuarioDenunciado,
        @NotBlank String motivo,
        String descripcion
) {
}