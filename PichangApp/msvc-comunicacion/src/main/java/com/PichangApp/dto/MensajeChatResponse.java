package com.PichangApp.dto;

import java.time.LocalDateTime;

import com.PichangApp.model.enums.TipoMensaje;

public record MensajeChatResponse(
        Long id,
        Long salaId,
        Long remitenteId,
        String contenido,
        TipoMensaje tipoMensaje,
        String mediaUrl,
        LocalDateTime fechaEnvio
) {}
