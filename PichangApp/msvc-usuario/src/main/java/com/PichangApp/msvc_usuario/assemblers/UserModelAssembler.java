package com.PichangApp.msvc.usuario.assemblers;

import com.PichangApp.msvc.usuario.controllers.UserController;
import com.PichangApp.msvc.usuario.models.entities.User;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

    @Override
    public EntityModel<User> toModel(User user) {
        return EntityModel.of(user,
                linkTo(methodOn(UserController.class).findById(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).findAll()).withRel("users")
        );
    }
}