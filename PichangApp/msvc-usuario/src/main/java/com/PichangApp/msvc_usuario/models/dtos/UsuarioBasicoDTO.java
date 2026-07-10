package com.PichangApp.msvc_usuario.models.dtos;

public record UsuarioBasicoDTO(
        Long id,
        String username,
        String nombre,
        String apellido,
        String email,
        String fotoUrl
) {
    public String nombreCompleto() {
        String nombreSeguro = nombre != null ? nombre : "";
        String apellidoSeguro = apellido != null ? apellido : "";
        String completo = (nombreSeguro + " " + apellidoSeguro).trim();

        if (!completo.isBlank()) {
            return completo;
        }

        if (username != null && !username.isBlank()) {
            return username;
        }

        return "Usuario " + id;
    }
}
