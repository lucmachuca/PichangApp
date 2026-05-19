package com.PichangApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO utilizado para recibir los datos necesarios para enviar un mensaje.
 * Este record encapsula los identificadores del match y del emisor así
 * como el contenido del mensaje.  Las anotaciones de validación
 * aseguran que se provea la información mínima requerida.
 */
public record MensajeRequest(
        @NotNull UUID idMatch,
        @NotNull UUID idEmisor,
        @NotBlank @Size(max = 2000) String contenido
) {}