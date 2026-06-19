package com.PichangApp.msvc_notificacion;

import com.PichangApp.msvc_notificacion.dto.CrearNotificacionRequest;
import com.PichangApp.msvc_notificacion.dto.NotificacionResponse;
import com.PichangApp.msvc_notificacion.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping("/estado")
    public ResponseEntity<String> estado() {
        return ResponseEntity.ok("msvc-notificacion activo.");
    }

    @PostMapping
    public ResponseEntity<NotificacionResponse> crear(
            @Valid @RequestBody CrearNotificacionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificacionService.crearNotificacion(request));
    }

    @GetMapping("/user/{usuarioId}")
    public ResponseEntity<List<NotificacionResponse>> listarPorUsuario(
            @PathVariable Long usuarioId
    ) {
        return ResponseEntity.ok(notificacionService.listarPorUsuario(usuarioId));
    }

    @PutMapping("/{id}/leida")
    public ResponseEntity<NotificacionResponse> marcarComoLeida(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
    }
}