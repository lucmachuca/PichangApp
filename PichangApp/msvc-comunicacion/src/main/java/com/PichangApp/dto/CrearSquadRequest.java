package com.PichangApp.dto;

import jakarta.validation.constraints.*;

public record CrearSquadRequest(
        @NotNull Long creadorId,
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 40) String deporte,
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull @Min(2) @Max(50) Integer maxIntegrantes,
        Double latitud,
        Double longitud
) {
}
