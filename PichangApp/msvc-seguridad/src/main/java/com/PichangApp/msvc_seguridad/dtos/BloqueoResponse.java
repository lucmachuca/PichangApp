package com.PichangApp.msvc_seguridad.dtos;

import java.time.LocalDateTime;

public record BloqueoResponse(
        Long idBloqueo,
        Long idUsuarioOrigen,
        Long idUsuarioBloqueado,
        LocalDateTime fechaBloqueo
) {
}