package com.PichangApp.dto;

import java.time.LocalDateTime;

public record MensajeCreadoEvent(
        Long mensajeId,
        Long salaId,
        Long matchSocialId,
        Long remitenteId,
        Long destinatarioId,
        String contenido,
        LocalDateTime fechaEnvio
) {
}
