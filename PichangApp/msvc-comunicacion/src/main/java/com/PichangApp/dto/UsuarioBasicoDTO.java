package com.PichangApp.dto;

public record UsuarioBasicoDTO(
        Long id,
        String username,
        String nombre,
        String apellido,
        String email
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