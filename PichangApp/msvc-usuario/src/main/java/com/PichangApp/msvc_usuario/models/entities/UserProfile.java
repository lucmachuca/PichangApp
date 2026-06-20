package com.PichangApp.msvc_usuario.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "users_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;

    private Integer edad;

    private String fotoUrl;

    private String deportePrincipal;

    private Double latitud;

    private Double longitud;

    private String ciudad;

    private String comuna;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> atributosDeportivos = new HashMap<>();

    @OneToOne(mappedBy = "profile")
    @JsonIgnore
    private User user;

    @PrePersist
    @PreUpdate
    public void validarPerfil() {
        if (atributosDeportivos == null) {
            atributosDeportivos = new HashMap<>();
        }

        if (deportePrincipal == null || deportePrincipal.isBlank()) {
            return;
        }

        String deporte = deportePrincipal.toUpperCase();

        if ("BASKET".equals(deporte)) {
            validarCampoObligatorio("altura");
            validarCampoObligatorio("posicion");
        }

        if ("BOXEO".equals(deporte)) {
            validarCampoObligatorio("peso");
            validarCampoObligatorio("guardia");
        }
    }

    private void validarCampoObligatorio(String campo) {
        if (!atributosDeportivos.containsKey(campo) || atributosDeportivos.get(campo) == null) {
            throw new IllegalArgumentException(
                    "Falta el atributo deportivo obligatorio: " + campo
            );
        }
    }
}