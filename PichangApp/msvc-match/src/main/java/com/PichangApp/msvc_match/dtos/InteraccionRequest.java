package com.PichangApp.msvc_match.dtos;

import com.PichangApp.msvc_match.models.enums.TipoInteraccion;
import jakarta.validation.constraints.NotNull;

public record InteraccionRequest(
        @NotNull Long usuarioOrigenId,
        @NotNull Long usuarioDestinoId,
        @NotNull TipoInteraccion tipo
) {}
