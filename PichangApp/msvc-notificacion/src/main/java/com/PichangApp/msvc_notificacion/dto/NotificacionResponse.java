package com.PichangApp.msvc_notificacion.dto;

import java.time.LocalDateTime;

public record NotificacionResponse(
        Long id,
        Long usuarioId,
        String titulo,
        String mensaje,
        String tipo,
        Boolean leida,
        LocalDateTime fechaCreacion
) {
}