package com.PichangApp.dto;

public record SquadSolicitudCreadaEvent(
        Long solicitudId,
        Long squadId,
        String squadNombre,
        Long solicitanteId,
        Long adminId
) {
}

