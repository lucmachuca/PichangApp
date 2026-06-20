package com.PichangApp.dto;

import java.time.LocalDateTime;

public record BloqueoEliminadoEvent(
        Long idUsuarioOrigen,
        Long idUsuarioBloqueado,
        LocalDateTime fechaEliminacion
) {
}