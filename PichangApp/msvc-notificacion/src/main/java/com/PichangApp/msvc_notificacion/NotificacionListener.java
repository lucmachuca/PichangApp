package com.PichangApp.msvc_notificacion;

import com.PichangApp.dto.MatchCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificacionListener {

    private final UsuarioFeignClient usuarioFeignClient;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICACIONES_MATCH)
    public void recibirEventoMatch(MatchCreatedEvent evento) {

        System.out.println("✅ [RabbitMQ] Evento recibido: Match creado entre "
                + evento.usuarioAId() + " y " + evento.usuarioBId());

        try {
            var usuario = usuarioFeignClient.obtenerUsuarioBasico(evento.usuarioAId());
            System.out.println("✅ [Feign] Usuario consultado: " + usuario);
        } catch (Exception e) {
            System.err.println("⚠️ [Feign] Error al consultar usuario: " + e.getMessage());
        }
    }
}