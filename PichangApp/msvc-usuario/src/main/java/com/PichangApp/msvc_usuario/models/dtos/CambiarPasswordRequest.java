package com.PichangApp.msvc_usuario.models.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CambiarPasswordRequest {

    @NotBlank
    private String passwordActual;

    @NotBlank
    private String nuevaPassword;

    @NotBlank
    private String confirmarNuevaPassword;
}
