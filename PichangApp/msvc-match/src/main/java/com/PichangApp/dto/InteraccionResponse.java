package com.PichangApp.dto;

import com.PichangApp.model.enums.TipoInteraccion;

import java.time.LocalDateTime;

public record InteraccionResponse(
                Long id,
                Long usuarioOrigenId,
                Long usuarioDestinoId,
                TipoInteraccion tipo,
                boolean hayMatch,
                LocalDateTime fechaCreacion) {
}
