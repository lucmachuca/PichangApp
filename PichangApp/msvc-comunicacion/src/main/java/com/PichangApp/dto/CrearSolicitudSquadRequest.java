package com.PichangApp.dto;

import jakarta.validation.constraints.NotNull;

public record CrearSolicitudSquadRequest(
        @NotNull Long usuarioId
) {
}