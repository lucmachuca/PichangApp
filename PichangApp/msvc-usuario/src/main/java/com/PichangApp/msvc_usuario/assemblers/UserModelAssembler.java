package com.PichangApp.msvc_usuario.assemblers;

import com.PichangApp.msvc_usuario.controllers.UserController;
import com.PichangApp.msvc_usuario.models.dtos.UserProfileDTO;
import com.PichangApp.msvc_usuario.models.dtos.UserResponseDTO;
import com.PichangApp.msvc_usuario.models.entities.User;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<UserResponseDTO>> {

    @Override
    public EntityModel<UserResponseDTO> toModel(User user) {
        // 1. Mapear datos seguros de User a DTO (La contraseña se queda atrás)
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setNombre(user.getNombre());
        dto.setApellido(user.getApellido());
        dto.setEnabled(user.isEnabled());

        // 2. Mapear el perfil si existe
        if (user.getProfile() != null) {
            UserProfileDTO profileDTO = new UserProfileDTO();
            profileDTO.setDescripcion(user.getProfile().getDescripcion());
            profileDTO.setEdad(user.getProfile().getEdad());
            profileDTO.setFotoUrl(user.getProfile().getFotoUrl());
            profileDTO.setDeportePrincipal(user.getProfile().getDeportePrincipal());
            profileDTO.setAtributosDeportivos(user.getProfile().getAtributosDeportivos());
            dto.setProfile(profileDTO);
        }

        // 3. Ensamblar DTO seguro con enlaces HATEOAS
        return EntityModel.of(dto,
                linkTo(methodOn(UserController.class).findById(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).findAll()).withRel("users")
        );
    }
}