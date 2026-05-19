package com.PichangApp.msvc.usuario.controllers;

import com.PichangApp.msvc.usuario.assemblers.UserModelAssembler;
import com.PichangApp.msvc.usuario.models.dtos.UserResponseDTO;
import com.PichangApp.msvc.usuario.models.entities.User;
import com.PichangApp.msvc.usuario.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "Usuarios", description = "Endpoints para la gestión de usuarios, perfiles deportivos y HATEOAS")
public class UserController {

    private final UserService userService;
    private final UserModelAssembler userModelAssembler;

    @Autowired
    public UserController(UserService userService, UserModelAssembler userModelAssembler) {
        this.userService = userService;
        this.userModelAssembler = userModelAssembler;
    }

    @Operation(summary = "Obtiene todos los usuarios del sistema")
    @ApiResponse(responseCode = "200", description = "Usuarios listados exitosamente")
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

    @Operation(summary = "Obtiene un usuario por su ID")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente")
    @ApiResponse(responseCode = "404", description = "Usuario no existe")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDTO>> findById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);

        return user.map(value -> ResponseEntity.ok(userModelAssembler.toModel(value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Registra un nuevo usuario y su perfil deportivo")
    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o falta de atributos deportivos")
    @PostMapping
    public ResponseEntity<EntityModel<UserResponseDTO>> create(@Valid @RequestBody User user) {
        User newUser = this.userService.save(user);
        EntityModel<UserResponseDTO> entityModel = this.userModelAssembler.toModel(newUser);

        return ResponseEntity
                .created(linkTo(methodOn(UserController.class).findById(newUser.getId())).toUri())
                .body(entityModel);
    }

    @Operation(summary = "Elimina un usuario por su ID")
    @ApiResponse(responseCode = "204", description = "Usuario borrado exitosamente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtiene la configuración de campos requeridos según el deporte")
    @ApiResponse(responseCode = "200", description = "Configuración devuelta con éxito")
    @GetMapping("/profiles/config/{deporte}")
    public ResponseEntity<Map<String, Object>> getProfileConfig(@PathVariable String deporte) {
        Map<String, Object> config;

        switch (deporte.toUpperCase()) {
            case "BASKET":
                config = Map.of(
                        "deporte", "BASKET",
                        "campos_requeridos", List.of(
                                Map.of("nombre", "altura", "tipo", "number", "unidad", "cm"),
                                Map.of("nombre", "posicion", "tipo", "string", "opciones", List.of("Base", "Escolta", "Alero", "Ala-Pívot", "Pívot"))
                        )
                );
                break;
            case "BOXEO":
                config = Map.of(
                        "deporte", "BOXEO",
                        "campos_requeridos", List.of(
                                Map.of("nombre", "peso", "tipo", "number", "unidad", "kg"),
                                Map.of("nombre", "guardia", "tipo", "string", "opciones", List.of("Ortodoxa", "Zurda"))
                        )
                );
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of("error", "Deporte no soportado"));
        }

        return ResponseEntity.ok(config);
    }
}