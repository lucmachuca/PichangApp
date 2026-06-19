package com.PichangApp.controller;

import com.PichangApp.dto.EnviarMensajeRequest;
import com.PichangApp.service.SalaChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SalaChatService salaChatService;

    /**
     * Recibe los mensajes vía WebSocket STOMP.
     * Los clientes deben enviar a: /app/chat/{salaId}/sendMessage
     */
    @MessageMapping("/chat/{salaId}/sendMessage")
    public void sendMessage(
            @DestinationVariable Long salaId,
            @Payload EnviarMensajeRequest request) {
        
        // El servicio guarda en BD y automáticamente hace el broadcast a /topic/sala/{salaId}
        salaChatService.enviarMensaje(salaId, request);
    }
}
