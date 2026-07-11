package com.PichangApp.dto;

public record SquadSolicitudAceptadaEvent(
        Long solicitudId,
        Long squadId,
        String squadNombre,
        Long usuarioId,
        Long adminId
) {
}
