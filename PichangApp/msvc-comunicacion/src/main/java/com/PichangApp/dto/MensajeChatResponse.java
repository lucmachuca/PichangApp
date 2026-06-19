package com.PichangApp.dto;

import com.PichangApp.model.enums.TipoMensaje;

import java.time.LocalDateTime;

public record MensajeChatResponse(
        Long id,
        Long salaId,
        Long remitenteId,
        String remitenteNombre,
        String remitenteUsername,
        String contenido,
        TipoMensaje tipoMensaje,
        String mediaUrl,
        LocalDateTime fechaEnvio
) {
}