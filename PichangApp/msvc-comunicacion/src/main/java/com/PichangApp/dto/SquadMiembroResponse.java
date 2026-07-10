package com.PichangApp.dto;

import com.PichangApp.model.enums.RolSquad;

import java.time.LocalDateTime;

public record SquadMiembroResponse(
        Long id,
        Long squadId,
        Long usuarioId,
        RolSquad rol,
        LocalDateTime fechaUnion
) {
}