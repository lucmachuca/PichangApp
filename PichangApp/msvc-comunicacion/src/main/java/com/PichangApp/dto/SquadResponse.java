package com.PichangApp.dto;

import com.PichangApp.model.enums.EstadoSquad;

import java.time.LocalDateTime;

public record SquadResponse(
        Long id,
        Long creadorId,
        String nombre,
        String deporte,
        String descripcion,
        Integer maxIntegrantes,
        Integer integrantesActuales,
        Double latitud,
        Double longitud,
        Double distanciaKm,
        EstadoSquad estado,
        LocalDateTime fechaCreacion,
        Boolean esMiembro,
        Boolean solicitudPendiente
) {
}
