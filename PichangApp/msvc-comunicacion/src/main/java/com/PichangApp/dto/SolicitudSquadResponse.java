package com.PichangApp.dto;

import com.PichangApp.model.enums.EstadoSolicitudSquad;

import java.time.LocalDateTime;

public record SolicitudSquadResponse(
        Long id,
        Long squadId,
        Long usuarioId,
        EstadoSolicitudSquad estado,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaRespuesta
) {
}