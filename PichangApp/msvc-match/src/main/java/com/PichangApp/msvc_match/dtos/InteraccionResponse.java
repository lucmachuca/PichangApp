package com.PichangApp.msvc_match.dtos;

import com.PichangApp.msvc_match.models.enums.TipoInteraccion;

import java.time.LocalDateTime;

public record InteraccionResponse(
                Long id,
                Long usuarioOrigenId,
                Long usuarioDestinoId,
                TipoInteraccion tipo,
                boolean hayMatch,
                LocalDateTime fechaCreacion) {
}
