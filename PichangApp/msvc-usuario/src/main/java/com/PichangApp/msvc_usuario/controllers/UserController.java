package com.PichangApp.msvc_usuario.controllers;

import com.PichangApp.msvc_usuario.assemblers.UserModelAssembler;
import com.PichangApp.msvc_usuario.models.dtos.UserResponseDTO;
import com.PichangApp.msvc_usuario.models.dtos.UsuarioBasicoDTO;
import com.PichangApp.msvc_usuario.models.dtos.UpdateUserProfileDTO;
import com.PichangApp.msvc_usuario.models.entities.UserProfile;
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

    public UserController(
            UserService userService,
            UserModelAssembler userModelAssembler
    ) {
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

        // Validar que venga el usuario actual
        if (excludeId == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> usuarioActualOpt = userService.findById(excludeId);

        if (usuarioActualOpt.isEmpty() || usuarioActualOpt.get().getProfile() == null) {
            return ResponseEntity.badRequest().build();
        }

        User usuarioActual = usuarioActualOpt.get();

        Double miLat = usuarioActual.getProfile().getLatitud();
        Double miLng = usuarioActual.getProfile().getLongitud();
        String miDeporte = usuarioActual.getProfile().getDeportePrincipal();

        if (miLat == null || miLng == null || miDeporte == null) {
            return ResponseEntity.ok(List.of());
        }

        // Para pruebas puedes usar 50 km.
        // Para producción puedes volver a 20 km.
        double radioKm = 50.0;

        List<UserResponseDTO> users = userService.findAll()
                .stream()

                // Usuario habilitado
                .filter(User::isEnabled)

                // No mostrarme a mí
                .filter(user -> !user.getId().equals(excludeId))

                // Debe tener perfil
                .filter(user -> user.getProfile() != null)

                // Debe tener deporte
                .filter(user -> user.getProfile().getDeportePrincipal() != null)

                // Debe tener ubicación
                .filter(user ->
                        user.getProfile().getLatitud() != null &&
                                user.getProfile().getLongitud() != null)

                // Mismo deporte
                .filter(user ->
                        miDeporte.equalsIgnoreCase(
                                user.getProfile().getDeportePrincipal()
                        ))

                // Distancia
                .filter(user -> {

                    double distancia = calcularDistanciaKm(
                            miLat,
                            miLng,
                            user.getProfile().getLatitud(),
                            user.getProfile().getLongitud()
                    );

                    return distancia <= radioKm;
                })

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
                            Map.of(
                                    "nombre", "altura",
                                    "tipo", "number",
                                    "unidad", "cm"
                            ),
                            Map.of(
                                    "nombre", "posicion",
                                    "tipo", "string",
                                    "opciones", List.of(
                                            "Base",
                                            "Escolta",
                                            "Alero",
                                            "Ala-Pívot",
                                            "Pívot"
                                    )
                            )
                    )
            );
            case "BOXEO" -> config = Map.of(
                    "deporte", "BOXEO",
                    "campos_requeridos", List.of(
                            Map.of(
                                    "nombre", "peso",
                                    "tipo", "number",
                                    "unidad", "kg"
                            ),
                            Map.of(
                                    "nombre", "guardia",
                                    "tipo", "string",
                                    "opciones", List.of(
                                            "Ortodoxa",
                                            "Zurda"
                                    )
                            )
                    )
            );
            default -> {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Deporte no soportado"));
            }
        }

        return ResponseEntity.ok(config);
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateUserProfileDTO updateProfileDTO,
            Authentication authentication) {

        try {
            // Validar que el usuario autenticado es el propietario del perfil
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isEmpty() || !userOpt.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Actualizar el perfil
            UserProfile updatedProfile = userService.updateUserProfile(id, updateProfileDTO);

            // Retornar la respuesta con el perfil actualizado
            Map<String, Object> response = Map.of(
                    "id", updatedProfile.getId(),
                    "descripcion", updatedProfile.getDescripcion(),
                    "edad", updatedProfile.getEdad(),
                    "deportePrincipal", updatedProfile.getDeportePrincipal(),
                    "atributosDeportivos", updatedProfile.getAtributosDeportivos(),
                    "latitud", updatedProfile.getLatitud(),
                    "longitud", updatedProfile.getLongitud(),
                    "mensaje", "Perfil actualizado correctamente"
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el perfil: " + e.getMessage()));
        }
    }

    private double calcularDistanciaKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        final int R = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}