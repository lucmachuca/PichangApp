package com.PichangApp.service;

import com.PichangApp.config.RabbitMQConfig;
import com.PichangApp.dto.BloqueoCreadoEvent;
import com.PichangApp.model.BloqueoUsuario;
import com.PichangApp.repository.BloqueoUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloqueoEventListener {

    private final BloqueoUsuarioRepository bloqueoUsuarioRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_BLOQUEO_CREADO)
    public void handleBloqueoCreado(BloqueoCreadoEvent event) {
        log.info(
                "Procesando BloqueoCreadoEvent: bloqueoId={}, origen={}, bloqueado={}",
                event.idBloqueo(),
                event.idUsuarioOrigen(),
                event.idUsuarioBloqueado()
        );

        boolean yaExiste = bloqueoUsuarioRepository
                .existsByIdUsuarioOrigenAndIdUsuarioBloqueado(
                        event.idUsuarioOrigen(),
                        event.idUsuarioBloqueado()
                );

        if (yaExiste) {
            log.warn(
                    "El bloqueo origen={} bloqueado={} ya existe en la cache local de comunicación. Se omite.",
                    event.idUsuarioOrigen(),
                    event.idUsuarioBloqueado()
            );
            return;
        }

        BloqueoUsuario bloqueo = new BloqueoUsuario();
        bloqueo.setIdBloqueoOriginal(event.idBloqueo());
        bloqueo.setIdUsuarioOrigen(event.idUsuarioOrigen());
        bloqueo.setIdUsuarioBloqueado(event.idUsuarioBloqueado());
        bloqueo.setFechaBloqueo(event.fechaBloqueo());

        bloqueoUsuarioRepository.save(bloqueo);

        log.info(
                "Bloqueo guardado en cache local de comunicación. origen={}, bloqueado={}",
                event.idUsuarioOrigen(),
                event.idUsuarioBloqueado()
        );
    }
}