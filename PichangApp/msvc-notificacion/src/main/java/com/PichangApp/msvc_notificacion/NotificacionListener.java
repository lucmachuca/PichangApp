package com.PichangApp.msvc_notificacion;

import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificacionListener {

    // 1. Inyectamos Feign para la consulta SÍNCRONA
    private final UsuarioFeignClient usuarioFeignClient;

    // 2. Escuchamos ASÍNCRONAMENTE el evento de RabbitMQ
    // queuesToDeclare crea la cola automáticamente sin necesidad de archivos de configuración extra.
    @RabbitListener(queuesToDeclare = @Queue("queue_notificaciones_match"))
    public void recibirEventoMatch(MatchEventDto evento) {
        
        System.out.println("✅ [RabbitMQ] Evento asíncrono recibido: Match creado entre " + evento.usuarioAId() + " y " + evento.usuarioBId());

        try {
            // 3. Usamos Feign para consultar datos SÍNCRONOS en tiempo real
            var usuario = usuarioFeignClient.obtenerUsuarioBasico(evento.usuarioAId());
            System.out.println("✅ [Feign] Consulta síncrona exitosa. Enviando email a: " + usuario.email());
            
        } catch (Exception e) {
            System.err.println("⚠️ [Feign] Error al consultar usuario (¿msvc-usuario está apagado?): " + e.getMessage());
        }
    }

    // Record interno ultra-minimalista para el evento RabbitMQ
    public record MatchEventDto(Long matchId, Long usuarioAId, Long usuarioBId) {}
}
