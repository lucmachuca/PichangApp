package com.PichangApp.dto;

import com.PichangApp.model.enums.TipoInteraccion;
import jakarta.validation.constraints.NotNull;

public record InteraccionRequest(
        @NotNull Long usuarioOrigenId,
        @NotNull Long usuarioDestinoId,
        @NotNull TipoInteraccion tipo
) {}
