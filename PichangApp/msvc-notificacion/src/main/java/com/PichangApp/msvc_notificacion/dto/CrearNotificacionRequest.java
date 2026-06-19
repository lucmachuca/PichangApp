package com.PichangApp.msvc_notificacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearNotificacionRequest(
        @NotNull Long usuarioId,
        @NotBlank String titulo,
        @NotBlank String mensaje,
        @NotBlank String tipo
) {
}