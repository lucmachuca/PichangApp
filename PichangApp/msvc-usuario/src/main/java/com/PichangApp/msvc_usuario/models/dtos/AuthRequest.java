package com.PichangApp.msvc_usuario.models.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO simple para el login. El filtro de autenticación solo necesita username
 * y password; no debe deserializar la entidad User completa.
 */
public class AuthRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
