package com.PichangApp.msvc_usuario.models.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class UpdateUserProfileDTO {

    private String descripcion;
    private Integer edad;
    private String sexo;
    private String deportePrincipal;
    private Map<String, Object> atributosDeportivos;
    private Double latitud;
    private Double longitud;

    public UpdateUserProfileDTO(
            String descripcion,
            Integer edad,
            String sexo,
            String deportePrincipal,
            Map<String, ?> atributosDeportivos,
            Double latitud,
            Double longitud
    ) {
        this.descripcion = descripcion;
        this.edad = edad;
        this.sexo = sexo;
        this.deportePrincipal = deportePrincipal;
        this.atributosDeportivos = copiarAtributos(atributosDeportivos);
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public UpdateUserProfileDTO(
            String descripcion,
            Integer edad,
            String deportePrincipal,
            Map<String, ?> atributosDeportivos,
            Double latitud,
            Double longitud
    ) {
        this(
                descripcion,
                edad,
                null,
                deportePrincipal,
                atributosDeportivos,
                latitud,
                longitud
        );
    }

    private Map<String, Object> copiarAtributos(Map<String, ?> atributos) {
        if (atributos == null) {
            return null;
        }

        return new HashMap<>(atributos);
    }
}