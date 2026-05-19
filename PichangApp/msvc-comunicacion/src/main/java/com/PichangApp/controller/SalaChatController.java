package com.PichangApp.controller;

import com.PichangApp.dto.*;
import com.PichangApp.service.SalaChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class SalaChatController {

    private final SalaChatService salaChatService;

    @PostMapping("/salas")
    public ResponseEntity<SalaChatResponse> crearSala(@Valid @RequestBody CrearSalaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaChatService.crearSala(request));
    }

    @GetMapping("/salas/user/{userId}")
    public ResponseEntity<List<SalaChatResponse>> getUserRooms(@PathVariable Long userId) {
        return ResponseEntity.ok(salaChatService.obtenerSalasPorUsuario(userId));
    }

    @PostMapping("/salas/{salaId}/mensajes")
    public ResponseEntity<MensajeChatResponse> enviarMensaje(
            @PathVariable Long salaId,
            @Valid @RequestBody EnviarMensajeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaChatService.enviarMensaje(salaId, request));
    }

    @GetMapping("/salas/{salaId}/mensajes")
    public ResponseEntity<Page<MensajeChatResponse>> obtenerMensajes(
            @PathVariable Long salaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(salaChatService.obtenerMensajes(salaId, page, size));
    }
}
