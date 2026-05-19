package com.PichangApp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida que representa un mensaje almacenado.  Se utiliza para
 * serializar las respuestas de la API sin exponer la entidad JPA.
 */
public record MensajeResponse(
        UUID idMensaje,
        UUID idMatch,
        UUID idEmisor,
        String contenido,
        LocalDateTime fechaEnvio,
        Boolean leido
) {}