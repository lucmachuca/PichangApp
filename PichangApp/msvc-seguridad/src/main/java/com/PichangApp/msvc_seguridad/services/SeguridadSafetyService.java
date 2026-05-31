package com.PichangApp.msvc_seguridad.services;

import com.PichangApp.dto.BloqueoCreadoEvent;
import com.PichangApp.msvc_seguridad.config.RabbitMQConfig;
import com.PichangApp.msvc_seguridad.dtos.BloqueoRequest;
import com.PichangApp.msvc_seguridad.dtos.BloqueoResponse;
import com.PichangApp.msvc_seguridad.dtos.ReporteRequest;
import com.PichangApp.msvc_seguridad.dtos.ReporteResponse;
import com.PichangApp.msvc_seguridad.models.Bloqueo;
import com.PichangApp.msvc_seguridad.models.Reporte;
import com.PichangApp.msvc_seguridad.repositories.BloqueoRepository;
import com.PichangApp.msvc_seguridad.repositories.ReporteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@Slf4j
public class SeguridadSafetyService {

    private final ReporteRepository reporteRepository;
    private final BloqueoRepository bloqueoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;

    public SeguridadSafetyService(
            ReporteRepository reporteRepository,
            BloqueoRepository bloqueoRepository,
            ApplicationEventPublisher eventPublisher,
            RabbitTemplate rabbitTemplate
    ) {
        this.reporteRepository = reporteRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.eventPublisher = eventPublisher;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public ReporteResponse reportarUsuario(ReporteRequest request) {
        if (request.idUsuarioDenunciante().equals(request.idUsuarioDenunciado())) {
            throw new IllegalArgumentException("Un usuario no puede reportarse a sí mismo.");
        }

        Reporte reporte = Reporte.builder()
                .idUsuarioDenunciante(request.idUsuarioDenunciante())
                .idUsuarioDenunciado(request.idUsuarioDenunciado())
                .motivo(request.motivo())
                .descripcion(request.descripcion())
                .estado("PENDIENTE")
                .build();

        return toReporteResponse(reporteRepository.save(reporte));
    }

    @Transactional
    public BloqueoResponse bloquearUsuario(BloqueoRequest request) {
        if (request.idUsuarioOrigen().equals(request.idUsuarioBloqueado())) {
            throw new IllegalArgumentException("Un usuario no puede bloquearse a sí mismo.");
        }

        bloqueoRepository.findByIdUsuarioOrigenAndIdUsuarioBloqueado(
                request.idUsuarioOrigen(),
                request.idUsuarioBloqueado()
        ).ifPresent(bloqueo -> {
            throw new IllegalStateException("El usuario ya se encuentra bloqueado.");
        });

        Bloqueo bloqueo = Bloqueo.builder()
                .idUsuarioOrigen(request.idUsuarioOrigen())
                .idUsuarioBloqueado(request.idUsuarioBloqueado())
                .build();

        Bloqueo bloqueoGuardado = bloqueoRepository.save(bloqueo);

        BloqueoCreadoEvent event = new BloqueoCreadoEvent(
                bloqueoGuardado.getIdBloqueo(),
                bloqueoGuardado.getIdUsuarioOrigen(),
                bloqueoGuardado.getIdUsuarioBloqueado(),
                bloqueoGuardado.getFechaBloqueo()
        );

        eventPublisher.publishEvent(event);

        log.info(
                "BloqueoCreadoEvent registrado localmente para envío diferido. bloqueoId={}, origen={}, bloqueado={}",
                event.idBloqueo(),
                event.idUsuarioOrigen(),
                event.idUsuarioBloqueado()
        );

        return toBloqueoResponse(bloqueoGuardado);
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> listarReportes() {
        return reporteRepository.findAll()
                .stream()
                .map(this::toReporteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BloqueoResponse> listarBloqueosPorUsuario(Long idUsuario) {
        return bloqueoRepository.findByIdUsuarioOrigen(idUsuario)
                .stream()
                .map(this::toBloqueoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existeBloqueoEntreUsuarios(Long usuarioAId, Long usuarioBId) {
        boolean usuarioABloqueoUsuarioB = bloqueoRepository
                .findByIdUsuarioOrigenAndIdUsuarioBloqueado(usuarioAId, usuarioBId)
                .isPresent();

        boolean usuarioBBloqueoUsuarioA = bloqueoRepository
                .findByIdUsuarioOrigenAndIdUsuarioBloqueado(usuarioBId, usuarioAId)
                .isPresent();

        return usuarioABloqueoUsuarioB || usuarioBBloqueoUsuarioA;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBloqueoCreado(BloqueoCreadoEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_SEGURIDAD,
                    RabbitMQConfig.ROUTING_KEY_BLOQUEO_CREADO,
                    event
            );

            log.info(
                    "BloqueoCreadoEvent enviado con éxito a RabbitMQ. bloqueoId={}, origen={}, bloqueado={}",
                    event.idBloqueo(),
                    event.idUsuarioOrigen(),
                    event.idUsuarioBloqueado()
            );
        } catch (AmqpException e) {
            log.error(
                    "Error al enviar BloqueoCreadoEvent a RabbitMQ. bloqueoId={}, motivo={}",
                    event.idBloqueo(),
                    e.getMessage()
            );
        }
    }

    private ReporteResponse toReporteResponse(Reporte reporte) {
        return new ReporteResponse(
                reporte.getIdReporte(),
                reporte.getIdUsuarioDenunciante(),
                reporte.getIdUsuarioDenunciado(),
                reporte.getMotivo(),
                reporte.getDescripcion(),
                reporte.getEstado(),
                reporte.getFechaReporte()
        );
    }

    private BloqueoResponse toBloqueoResponse(Bloqueo bloqueo) {
        return new BloqueoResponse(
                bloqueo.getIdBloqueo(),
                bloqueo.getIdUsuarioOrigen(),
                bloqueo.getIdUsuarioBloqueado(),
                bloqueo.getFechaBloqueo()
        );
    }
}