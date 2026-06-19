package com.PichangApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.PichangApp.model.enums.TipoMensaje;

public record EnviarMensajeRequest(
        @NotNull Long remitenteId,
        @NotBlank @Size(max = 2000) String contenido,
        TipoMensaje tipoMensaje,
        String mediaUrl
) {}
