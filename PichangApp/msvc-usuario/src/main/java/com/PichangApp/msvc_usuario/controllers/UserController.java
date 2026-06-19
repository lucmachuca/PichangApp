package com.PichangApp.msvc_usuario.controllers;

import com.PichangApp.msvc_usuario.assemblers.UserModelAssembler;
import com.PichangApp.msvc_usuario.models.dtos.UserResponseDTO;
import com.PichangApp.msvc_usuario.models.entities.User;
import com.PichangApp.msvc_usuario.services.UserService;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.PichangApp.msvc_usuario.models.dtos.UsuarioBasicoDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@Validated
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final UserModelAssembler userModelAssembler;

    public UserController(UserService userService, UserModelAssembler userModelAssembler) {
        this.userService = userService;
        this.userModelAssembler = userModelAssembler;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDTO>>> findAll() {
        List<EntityModel<UserResponseDTO>> entityModels = this.userService.findAll()
                .stream()
                .map(userModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<UserResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(UserController.class).findAll()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<UsuarioBasicoDTO> findBasicById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(new UsuarioBasicoDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getNombre(),
                        user.getApellido(),
                        user.getEmail()
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDTO>> findById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);

        return user.map(value -> ResponseEntity.ok(userModelAssembler.toModel(value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<EntityModel<UserResponseDTO>> create(@Valid @RequestBody User user) {
        User newUser = this.userService.save(user);
        EntityModel<UserResponseDTO> entityModel = this.userModelAssembler.toModel(newUser);

        return ResponseEntity
                .created(linkTo(methodOn(UserController.class).findById(newUser.getId())).toUri())
                .body(entityModel);
    }

    @PostMapping("/register")
    public ResponseEntity<EntityModel<UserResponseDTO>> register(@Valid @RequestBody User user) {
        return create(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userService.findByUsername(authentication.getName())
                .map(user -> ResponseEntity.ok(userModelAssembler.toDto(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/discover")
    public ResponseEntity<List<UserResponseDTO>> discoverUsers(
            @RequestParam(required = false) Long excludeId
    ) {
        List<UserResponseDTO> users = userService.findAll()
                .stream()
                .filter(User::isEnabled)
                .filter(user -> excludeId == null || !user.getId().equals(excludeId))
                .map(userModelAssembler::toDto)
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/profiles/config/{deporte}")
    public ResponseEntity<Map<String, Object>> getProfileConfig(@PathVariable String deporte) {
        Map<String, Object> config;

        switch (deporte.toUpperCase()) {
            case "BASKET" -> config = Map.of(
                    "deporte", "BASKET",
                    "campos_requeridos", List.of(
                            Map.of("nombre", "altura", "tipo", "number", "unidad", "cm"),
                            Map.of("nombre", "posicion", "tipo", "string", "opciones", List.of("Base", "Escolta", "Alero", "Ala-Pívot", "Pívot"))
                    )
            );
            case "BOXEO" -> config = Map.of(
                    "deporte", "BOXEO",
                    "campos_requeridos", List.of(
                            Map.of("nombre", "peso", "tipo", "number", "unidad", "kg"),
                            Map.of("nombre", "guardia", "tipo", "string", "opciones", List.of("Ortodoxa", "Zurda"))
                    )
            );
            default -> {
                return ResponseEntity.badRequest().body(Map.of("error", "Deporte no soportado"));
            }
        }

        return ResponseEntity.ok(config);
    }
}