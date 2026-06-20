package com.PichangApp.msvc_seguridad.controllers;

import com.PichangApp.msvc_seguridad.dtos.BloqueoRequest;
import com.PichangApp.msvc_seguridad.dtos.BloqueoResponse;
import com.PichangApp.msvc_seguridad.dtos.ReporteRequest;
import com.PichangApp.msvc_seguridad.dtos.ReporteResponse;
import com.PichangApp.msvc_seguridad.services.SeguridadSafetyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/safety")
public class SeguridadSafetyController {

    private final SeguridadSafetyService seguridadSafetyService;

    public SeguridadSafetyController(SeguridadSafetyService seguridadSafetyService) {
        this.seguridadSafetyService = seguridadSafetyService;
    }

    @GetMapping("/estado")
    public ResponseEntity<String> estadoServicio() {
        return ResponseEntity.ok("Servicio de Safety activo: Gestionando modo seguro, reportes y bloqueos.");
    }

    @PostMapping("/reportar")
    public ResponseEntity<ReporteResponse> reportarUsuario(@Valid @RequestBody ReporteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seguridadSafetyService.reportarUsuario(request));
    }

    @PostMapping("/bloquear")
    public ResponseEntity<BloqueoResponse> bloquearUsuario(@Valid @RequestBody BloqueoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seguridadSafetyService.bloquearUsuario(request));
    }

    @GetMapping("/reportes")
    public ResponseEntity<List<ReporteResponse>> listarReportes() {
        return ResponseEntity.ok(seguridadSafetyService.listarReportes());
    }

    @GetMapping("/bloqueos/user/{idUsuario}")
    public ResponseEntity<List<BloqueoResponse>> listarBloqueosPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(seguridadSafetyService.listarBloqueosPorUsuario(idUsuario));
    }

    @GetMapping("/bloqueos/existe-entre")
    public ResponseEntity<Boolean> existeBloqueoEntreUsuarios(
            @RequestParam Long usuarioAId,
            @RequestParam Long usuarioBId
    ) {
        return ResponseEntity.ok(
                seguridadSafetyService.existeBloqueoEntreUsuarios(usuarioAId, usuarioBId)
        );
    }

    @PostMapping("/desbloquear")
    public ResponseEntity<String> desbloquearUsuario(@Valid @RequestBody BloqueoRequest request) {
        seguridadSafetyService.desbloquearUsuario(request);
        return ResponseEntity.ok("Usuario desbloqueado exitosamente.");
    }
}