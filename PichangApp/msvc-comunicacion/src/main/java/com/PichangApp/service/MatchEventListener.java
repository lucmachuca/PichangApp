package com.PichangApp.service;

import com.PichangApp.config.RabbitMQConfig;
import com.PichangApp.dto.CrearSalaRequest;
import com.PichangApp.dto.MatchCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchEventListener {

    private final SalaChatService salaChatService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_MATCH_CREATED)
    public void handleMatchCreatedEvent(MatchCreatedEvent event) {
        log.info("Procesando MatchCreatedEvent: matchId={}, usuarioA={}, usuarioB={}",
                event.matchId(), event.usuarioAId(), event.usuarioBId());
        
        try {
            CrearSalaRequest request = new CrearSalaRequest(
                    event.matchId(),
                    event.usuarioAId(),
                    event.usuarioBId()
            );
            
            salaChatService.crearSala(request);
            log.info("SalaChat persistida exitosamente para el matchId={}", event.matchId());
            
        } catch (IllegalStateException e) {
            log.warn("La sala para el matchId={} ya existía previamente. Se omite la creación.", event.matchId());
        } catch (Exception e) {
            log.error("Error inesperado al crear la sala para el matchId={}: {}", event.matchId(), e.getMessage());
            // En sistemas robustos, aquí se podría relanzar la excepción para que RabbitMQ 
            // intente de nuevo, o enviarlo a una Dead Letter Queue (DLQ).
        }
    }
}
