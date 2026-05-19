package com.pichangapp.comunicacion.dto;

import java.time.LocalDateTime;

public record MensajeChatResponse(
        Long id,
        Long salaId,
        Long remitenteId,
        String contenido,
        LocalDateTime fechaEnvio
) {}
