package com.PichangApp.dto;

import java.time.LocalDateTime;

public record MensajeSquadResponse(
        Long id,
        Long squadId,
        Long remitenteId,
        String contenido,
        LocalDateTime fechaEnvio
) {
}
