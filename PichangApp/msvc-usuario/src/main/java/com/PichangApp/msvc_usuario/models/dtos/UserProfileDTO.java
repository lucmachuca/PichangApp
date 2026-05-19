package com.PichangApp.msvc_usuario.models.dtos;

import lombok.Data;
import java.util.Map;

@Data
public class UserProfileDTO {
    private String descripcion;
    private Integer edad;
    private String fotoUrl;
    private String deportePrincipal;
    private Map<String, Object> atributosDeportivos;
}