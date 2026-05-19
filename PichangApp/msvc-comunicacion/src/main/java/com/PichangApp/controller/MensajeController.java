package com.PichangApp.controller;

import com.PichangApp.dto.MensajeRequest;
import com.PichangApp.dto.MensajeResponse;
import com.PichangApp.service.MensajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para las operaciones de mensajería.  Define los
 * endpoints de creación y consulta de mensajes.  Cada llamada valida
 * automáticamente el token JWT gracias al filtro configurado en
 * SecurityConfig.
 */
@RestController
@RequestMapping("/api/mensajes")
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajeService;

    /**
     * Endpoint para enviar un mensaje.  Recibe un cuerpo JSON con los
     * identificadores del match y del emisor, junto con el contenido.
     *
     * @param request datos del mensaje
     * @return el mensaje persistido
     */
    @PostMapping("/enviar")
    public ResponseEntity<MensajeResponse> enviarMensaje(@Valid @RequestBody MensajeRequest request) {
        MensajeResponse response = mensajeService.enviarMensaje(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para listar los mensajes asociados a un MatchSocial.  El
     * identificador del match se indica como parte de la ruta.
     *
     * @param idMatch identificador del match
     * @return lista de mensajes
     */
    @GetMapping("/{idMatch}")
    public ResponseEntity<List<MensajeResponse>> obtenerMensajes(@PathVariable UUID idMatch) {
        List<MensajeResponse> mensajes = mensajeService.listarMensajes(idMatch);
        return ResponseEntity.ok(mensajes);
    }
}