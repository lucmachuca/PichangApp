package com.PichangApp.msvc_notificacion;

import com.PichangApp.dto.MatchCreatedEvent;
import com.PichangApp.msvc_notificacion.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionListener {

    private final UsuarioFeignClient usuarioFeignClient;
    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICACIONES_MATCH)
    public void recibirEventoMatch(MatchCreatedEvent evento) {
        log.info(
                "Evento recibido por RabbitMQ: Match creado entre {} y {}",
                evento.usuarioAId(),
                evento.usuarioBId()
        );

        UsuarioFeignClient.UsuarioBasicoDto usuarioA = obtenerUsuarioSeguro(evento.usuarioAId());
        UsuarioFeignClient.UsuarioBasicoDto usuarioB = obtenerUsuarioSeguro(evento.usuarioBId());

        notificacionService.crearNotificacion(
                evento.usuarioAId(),
                "Nuevo MatchSocial",
                "Hiciste match con " + usuarioB.nombreCompleto() + ". Ya pueden conversar.",
                "MATCH"
        );

        notificacionService.crearNotificacion(
                evento.usuarioBId(),
                "Nuevo MatchSocial",
                "Hiciste match con " + usuarioA.nombreCompleto() + ". Ya pueden conversar.",
                "MATCH"
        );

        log.info(
                "Notificaciones de match persistidas para usuarios {} y {}",
                evento.usuarioAId(),
                evento.usuarioBId()
        );
    }

    private UsuarioFeignClient.UsuarioBasicoDto obtenerUsuarioSeguro(Long id) {
        try {
            return usuarioFeignClient.obtenerUsuarioBasico(id);
        } catch (Exception e) {
            log.warn(
                    "No se pudo consultar usuario {} desde msvc-usuario. Se usará fallback. Motivo: {}",
                    id,
                    e.getMessage()
            );

            return new UsuarioFeignClient.UsuarioBasicoDto(
                    id,
                    "usuario" + id,
                    "Usuario",
                    String.valueOf(id),
                    null
            );
        }
    }
}