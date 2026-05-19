package com.PichangApp.dto;

import jakarta.validation.constraints.NotNull;

public record CrearSalaRequest(
        @NotNull Long matchSocialId,
        @NotNull Long usuarioAId,
        @NotNull Long usuarioBId
) {}
