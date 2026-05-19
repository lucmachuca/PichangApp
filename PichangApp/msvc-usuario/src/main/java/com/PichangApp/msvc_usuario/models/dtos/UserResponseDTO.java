package com.PichangApp.msvc_usuario.models.dtos;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private boolean enabled;
    private UserProfileDTO profile;
}