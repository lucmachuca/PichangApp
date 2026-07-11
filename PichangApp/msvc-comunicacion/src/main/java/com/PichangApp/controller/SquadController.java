package com.PichangApp.controller;

import com.PichangApp.dto.*;
import com.PichangApp.service.SquadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/squads")
@RequiredArgsConstructor
public class SquadController {

    private final SquadService squadService;

    @PostMapping
    public ResponseEntity<SquadResponse> crearSquad(
            @Valid @RequestBody CrearSquadRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(squadService.crearSquad(request));
    }

    @GetMapping("/discover")
    public ResponseEntity<List<SquadResponse>> descubrirSquads(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "Todos") String deporte,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            @RequestParam(defaultValue = "500") Double distanciaMaxKm
    ) {
        return ResponseEntity.ok(
                squadService.descubrirSquads(
                        usuarioId,
                        deporte,
                        latitud,
                        longitud,
                        distanciaMaxKm
                )
        );
    }

    @GetMapping("/mis-squads/{usuarioId}")
    public ResponseEntity<List<SquadResponse>> obtenerMisSquads(
            @PathVariable Long usuarioId
    ) {
        return ResponseEntity.ok(squadService.obtenerMisSquads(usuarioId));
    }

    @GetMapping("/{squadId}/miembros")
    public ResponseEntity<List<SquadMiembroResponse>> listarMiembros(
            @PathVariable Long squadId
    ) {
        return ResponseEntity.ok(squadService.listarMiembros(squadId));
    }

    @PostMapping("/{squadId}/solicitudes")
    public ResponseEntity<SolicitudSquadResponse> solicitarEntrada(
            @PathVariable Long squadId,
            @Valid @RequestBody CrearSolicitudSquadRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(squadService.solicitarEntrada(squadId, request));
    }

    @GetMapping("/{squadId}/solicitudes")
    public ResponseEntity<List<SolicitudSquadResponse>> listarSolicitudesPendientes(
            @PathVariable Long squadId,
            @RequestParam Long adminId
    ) {
        return ResponseEntity.ok(squadService.listarSolicitudesPendientes(squadId, adminId));
    }

    @PostMapping("/solicitudes/{solicitudId}/aceptar")
    public ResponseEntity<SolicitudSquadResponse> aceptarSolicitud(
            @PathVariable Long solicitudId,
            @RequestParam Long adminId
    ) {
        return ResponseEntity.ok(squadService.aceptarSolicitud(solicitudId, adminId));
    }

    @PostMapping("/solicitudes/{solicitudId}/rechazar")
    public ResponseEntity<SolicitudSquadResponse> rechazarSolicitud(
            @PathVariable Long solicitudId,
            @RequestParam Long adminId
    ) {
        return ResponseEntity.ok(squadService.rechazarSolicitud(solicitudId, adminId));
    }

    @PostMapping("/{squadId}/mensajes")
    public ResponseEntity<MensajeSquadResponse> enviarMensaje(
            @PathVariable Long squadId,
            @Valid @RequestBody EnviarMensajeSquadRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(squadService.enviarMensaje(squadId, request));
    }

    @GetMapping("/{squadId}/mensajes")
    public ResponseEntity<Page<MensajeSquadResponse>> obtenerMensajes(
            @PathVariable Long squadId,
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(squadService.obtenerMensajes(squadId, usuarioId, page, size));
    }

    @DeleteMapping("/{squadId}/miembros/{usuarioId}")
    public ResponseEntity<Void> expulsarMiembro(
            @PathVariable Long squadId,
            @PathVariable Long usuarioId,
            @RequestParam Long adminId
    ) {
        squadService.expulsarMiembro(squadId, adminId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
