package com.PichangApp.dto;

import java.time.LocalDateTime;

public record BloqueoCreadoEvent(
        Long idBloqueo,
        Long idUsuarioOrigen,
        Long idUsuarioBloqueado,
        LocalDateTime fechaBloqueo
) {
}