package com.PichangApp.msvc_usuario.models.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class UserProfileDTO {
    private String descripcion;
    private Integer edad;
    private String sexo;
    private String fotoUrl;
    private String deportePrincipal;
    private Map<String, Object> atributosDeportivos;
    private Double latitud;
    private Double longitud;
    private LocalDateTime ultimaUbicacionAt;
}