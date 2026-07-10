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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

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
            @RequestParam(required = false) Long excludeId,
            @RequestParam(defaultValue = "0") Double distanciaMinKm,
            @RequestParam(defaultValue = "500") Double distanciaMaxKm,
            @RequestParam(defaultValue = "0") Integer edadMin,
            @RequestParam(defaultValue = "120") Integer edadMax,
            @RequestParam(defaultValue = "TODOS") String sexo,
            @RequestParam(defaultValue = "30") Integer diasMaxUbicacion
    ) {

        if (excludeId == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> usuarioActualOpt = userService.findById(excludeId);

        if (usuarioActualOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User usuarioActual = usuarioActualOpt.get();
        UserProfile perfilActual = usuarioActual.getProfile();

        if (perfilActual == null) {
            return ResponseEntity.ok(List.of());
        }

        String miDeporte = normalizarTexto(perfilActual.getDeportePrincipal());
        Double miLat = perfilActual.getLatitud();
        Double miLng = perfilActual.getLongitud();

        if (miDeporte == null || !coordenadasValidas(miLat, miLng)) {
            return ResponseEntity.ok(List.of());
        }

        double minimoKm = normalizarDistancia(distanciaMinKm, 0.0);
        double maximoKm = normalizarDistancia(distanciaMaxKm, 50.0);

        if (minimoKm > maximoKm) {
            double temporal = minimoKm;
            minimoKm = maximoKm;
            maximoKm = temporal;
        }

        int edadMinima = normalizarEdad(edadMin, 0);
        int edadMaxima = normalizarEdad(edadMax, 120);

        if (edadMinima > edadMaxima) {
            int temporal = edadMinima;
            edadMinima = edadMaxima;
            edadMaxima = temporal;
        }

        int diasUbicacion = diasMaxUbicacion == null ? 30 : diasMaxUbicacion;

        if (diasUbicacion < 1) {
            diasUbicacion = 1;
        }

        if (diasUbicacion > 365) {
            diasUbicacion = 365;
        }

        final double distanciaMinimaFinal = minimoKm;
        final double distanciaMaximaFinal = maximoKm;
        final int edadMinimaFinal = edadMinima;
        final int edadMaximaFinal = edadMaxima;
        final String sexoFiltroFinal = normalizarSexoFiltro(sexo);
        final LocalDateTime fechaMinimaUbicacion = LocalDateTime.now().minusDays(diasUbicacion);

        List<UserResponseDTO> users = userService.findAll()
                .stream()
                .filter(User::isEnabled)
                .filter(user -> user.getId() != null && !user.getId().equals(excludeId))
                .filter(user -> user.getProfile() != null)
                .filter(user -> normalizarTexto(user.getProfile().getDeportePrincipal()) != null)
                .filter(user -> miDeporte.equals(
                        normalizarTexto(user.getProfile().getDeportePrincipal())
                ))
                .filter(user -> coordenadasValidas(
                        user.getProfile().getLatitud(),
                        user.getProfile().getLongitud()
                ))
                .filter(user -> ubicacionReciente(
                        user.getProfile().getUltimaUbicacionAt(),
                        fechaMinimaUbicacion
                ))
                .filter(user -> {
                    Integer edadUsuario = user.getProfile().getEdad();

                    return edadUsuario != null
                            && edadUsuario >= edadMinimaFinal
                            && edadUsuario <= edadMaximaFinal;
                })
                .filter(user -> {
                    if (sexoFiltroFinal == null) {
                        return true;
                    }

                    String sexoUsuario = normalizarTexto(user.getProfile().getSexo());

                    return sexoFiltroFinal.equals(sexoUsuario);
                })
                .filter(user -> {
                    double distancia = calcularDistanciaKm(
                            miLat,
                            miLng,
                            user.getProfile().getLatitud(),
                            user.getProfile().getLongitud()
                    );

                    return distancia >= distanciaMinimaFinal
                            && distancia <= distanciaMaximaFinal;
                })
                .map(userModelAssembler::toDto)
                .toList();

        return ResponseEntity.ok(users);
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim().toUpperCase();
    }

    private String normalizarSexoFiltro(String sexo) {
        String valor = normalizarTexto(sexo);

        if (valor == null || "TODOS".equals(valor)) {
            return null;
        }

        return valor;
    }

    private boolean coordenadasValidas(Double latitud, Double longitud) {
        if (latitud == null || longitud == null) {
            return false;
        }

        return latitud >= -90
                && latitud <= 90
                && longitud >= -180
                && longitud <= 180;
    }

    private double normalizarDistancia(Double distancia, double valorDefecto) {
        if (distancia == null || distancia.isNaN() || distancia.isInfinite()) {
            return valorDefecto;
        }

        if (distancia < 0) {
            return 0;
        }

        if (distancia > 500) {
            return 500;
        }

        return distancia;
    }

    private int normalizarEdad(Integer edad, int valorDefecto) {
        if (edad == null) {
            return valorDefecto;
        }

        if (edad < 0) {
            return 0;
        }

        if (edad > 120) {
            return 120;
        }

        return edad;
    }

    private boolean ubicacionReciente(LocalDateTime ultimaUbicacionAt, LocalDateTime fechaMinima) {
        if (ultimaUbicacionAt == null) {
            return false;
        }

        return !ultimaUbicacionAt.isBefore(fechaMinima);
    }

    @GetMapping("/profiles/config/{deporte}")
    public ResponseEntity<Map<String, Object>> getProfileConfig(@PathVariable String deporte) {
        String deporteNormalizado = deporte == null ? "" : deporte.trim().toUpperCase();

        if ("BOXEO".equals(deporteNormalizado)) {
            return ResponseEntity.ok(Map.of(
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
            ));
        }

        List<String> deportesConAlturaYPosicion = List.of(
                "BASKET",
                "FUTBOL",
                "FUTSAL",
                "TENIS",
                "PADEL",
                "VOLEIBOL",
                "RUNNING",
                "CICLISMO",
                "CALISTENIA",
                "TREKKING",
                "NATACION"
        );

        if (!deportesConAlturaYPosicion.contains(deporteNormalizado)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Deporte no soportado"));
        }

        return ResponseEntity.ok(Map.of(
                "deporte", deporteNormalizado,
                "campos_requeridos", List.of(
                        Map.of(
                                "nombre", "altura",
                                "tipo", "number",
                                "unidad", "cm"
                        ),
                        Map.of(
                                "nombre", "posicion",
                                "tipo", "string",
                                "opciones", opcionesPorDeporte(deporteNormalizado)
                        )
                )
        ));
    }

    private List<String> opcionesPorDeporte(String deporte) {
        return switch (deporte) {
            case "BASKET" -> List.of("Base", "Escolta", "Alero", "Ala Pívot", "Pívot");
            case "FUTBOL" -> List.of("Arquero", "Defensa", "Mediocampista", "Delantero");
            case "FUTSAL" -> List.of("Arquero", "Cierre", "Ala", "Pívot");
            case "TENIS" -> List.of("Singles", "Dobles", "Mixto", "Recreativo");
            case "PADEL" -> List.of("Drive", "Revés", "Ambos lados", "Recreativo");
            case "VOLEIBOL" -> List.of("Armador", "Punta", "Central", "Opuesto", "Líbero");
            case "RUNNING" -> List.of("5K", "10K", "Media maratón", "Maratón", "Trail");
            case "CICLISMO" -> List.of("Ruta", "MTB", "Urbano", "Gravel", "Recreativo");
            case "CALISTENIA" -> List.of("Principiante", "Intermedio", "Avanzado", "Street workout");
            case "TREKKING" -> List.of("Principiante", "Intermedio", "Avanzado", "Alta montaña");
            case "NATACION" -> List.of("Libre", "Espalda", "Pecho", "Mariposa", "Recreativo");
            default -> List.of("Recreativo");
        };
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
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", updatedProfile.getId());
            response.put("descripcion", updatedProfile.getDescripcion());
            response.put("edad", updatedProfile.getEdad());
            response.put("sexo", updatedProfile.getSexo());
            response.put("fotoUrl", updatedProfile.getFotoUrl());
            response.put("deportePrincipal", updatedProfile.getDeportePrincipal());
            response.put("atributosDeportivos", updatedProfile.getAtributosDeportivos());
            response.put("latitud", updatedProfile.getLatitud());
            response.put("longitud", updatedProfile.getLongitud());
            response.put("ultimaUbicacionAt", updatedProfile.getUltimaUbicacionAt());
            response.put("mensaje", "Perfil actualizado correctamente");

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