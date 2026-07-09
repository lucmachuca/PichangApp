package com.PichangApp.msvc_usuario.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileDTO {

    private String descripcion;
    private Integer edad;
    private String deportePrincipal;
    private Map<String, Object> atributosDeportivos;

    // NUEVO
    private Double latitud;
    private Double longitud;
}