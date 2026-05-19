package com.PichangApp.dto;

import java.time.LocalDateTime;

public record MensajeChatResponse(
        Long id,
        Long salaId,
        Long remitenteId,
        String contenido,
        LocalDateTime fechaEnvio
) {}
