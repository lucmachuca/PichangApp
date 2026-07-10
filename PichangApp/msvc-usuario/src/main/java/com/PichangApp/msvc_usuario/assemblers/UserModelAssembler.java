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
        UserResponseDTO dto = toDto(user);

        return EntityModel.of(dto,
                linkTo(methodOn(UserController.class).findById(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).findAll()).withRel("users")
        );
    }

    public UserResponseDTO toDto(User user) {
        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setNombre(user.getNombre());
        dto.setApellido(user.getApellido());
        dto.setEnabled(user.isEnabled());

        if (user.getProfile() != null) {
            UserProfileDTO profileDTO = new UserProfileDTO();
            profileDTO.setDescripcion(user.getProfile().getDescripcion());
            profileDTO.setEdad(user.getProfile().getEdad());
            profileDTO.setSexo(user.getProfile().getSexo());
            profileDTO.setFotoUrl(user.getProfile().getFotoUrl());
            profileDTO.setDeportePrincipal(user.getProfile().getDeportePrincipal());
            profileDTO.setAtributosDeportivos(user.getProfile().getAtributosDeportivos());
            profileDTO.setLatitud(user.getProfile().getLatitud());
            profileDTO.setLongitud(user.getProfile().getLongitud());
            profileDTO.setUltimaUbicacionAt(user.getProfile().getUltimaUbicacionAt());

            dto.setProfile(profileDTO);
        }

        return dto;
    }
}