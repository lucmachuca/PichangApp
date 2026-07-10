package com.PichangApp.service;

import com.PichangApp.config.RabbitMQConfig;
import com.PichangApp.dto.*;
import com.PichangApp.model.MensajeSquad;
import com.PichangApp.model.Squad;
import com.PichangApp.model.SquadMiembro;
import com.PichangApp.model.SquadSolicitud;
import com.PichangApp.model.enums.EstadoSolicitudSquad;
import com.PichangApp.model.enums.EstadoSquad;
import com.PichangApp.model.enums.RolSquad;
import com.PichangApp.repository.MensajeSquadRepository;
import com.PichangApp.repository.SquadMiembroRepository;
import com.PichangApp.repository.SquadRepository;
import com.PichangApp.repository.SquadSolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SquadService {

    private final SquadRepository squadRepository;
    private final SquadMiembroRepository squadMiembroRepository;
    private final SquadSolicitudRepository squadSolicitudRepository;
    private final MensajeSquadRepository mensajeSquadRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public SquadResponse crearSquad(CrearSquadRequest request) {
        String deporteNormalizado = normalizarTexto(request.deporte()).toUpperCase();

        Squad squad = Squad.builder()
                .creadorId(request.creadorId())
                .nombre(normalizarTexto(request.nombre()))
                .deporte(deporteNormalizado)
                .descripcion(normalizarTexto(request.descripcion()))
                .maxIntegrantes(request.maxIntegrantes())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .estado(EstadoSquad.ACTIVO)
                .build();

        squad = squadRepository.save(squad);

        SquadMiembro admin = SquadMiembro.builder()
                .squad(squad)
                .usuarioId(request.creadorId())
                .rol(RolSquad.ADMIN)
                .build();

        squadMiembroRepository.save(admin);

        log.info("Squad creado. squadId={}, creadorId={}", squad.getId(), request.creadorId());

        return toResponse(squad, request.creadorId(), null, null);
    }

    @Transactional(readOnly = true)
    public List<SquadResponse> descubrirSquads(
            Long usuarioId,
            String deporte,
            Double latitud,
            Double longitud,
            Double distanciaMaxKm
    ) {
        double distanciaMaxima = normalizarDistancia(distanciaMaxKm, 500);
        String deporteFiltro = deporte == null ? "TODOS" : deporte.trim();

        return squadRepository.findByEstadoOrderByFechaCreacionDesc(EstadoSquad.ACTIVO)
                .stream()
                .filter(squad -> !squadMiembroRepository.existsBySquadIdAndUsuarioId(squad.getId(), usuarioId))
                .filter(squad -> squadMiembroRepository.countBySquadId(squad.getId()) < squad.getMaxIntegrantes())
                .filter(squad -> deporteCoincide(squad.getDeporte(), deporteFiltro))
                .map(squad -> toResponse(squad, usuarioId, latitud, longitud))
                .filter(response -> response.distanciaKm() == null || response.distanciaKm() <= distanciaMaxima)
                .sorted(Comparator.comparing(
                        response -> response.distanciaKm() == null ? Double.MAX_VALUE : response.distanciaKm()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SquadResponse> obtenerMisSquads(Long usuarioId) {
        return squadMiembroRepository.findByUsuarioIdOrderByFechaUnionDesc(usuarioId)
                .stream()
                .map(SquadMiembro::getSquad)
                .map(squad -> toResponse(squad, usuarioId, null, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SquadMiembroResponse> listarMiembros(Long squadId) {
        asegurarSquadExiste(squadId);

        return squadMiembroRepository.findBySquadIdOrderByFechaUnionAsc(squadId)
                .stream()
                .map(this::toMiembroResponse)
                .toList();
    }

    @Transactional
    public SolicitudSquadResponse solicitarEntrada(Long squadId, CrearSolicitudSquadRequest request) {
        Squad squad = obtenerSquadActivo(squadId);

        if (squadMiembroRepository.existsBySquadIdAndUsuarioId(squadId, request.usuarioId())) {
            throw new IllegalStateException("Ya eres miembro de este squad.");
        }

        if (squadMiembroRepository.countBySquadId(squadId) >= squad.getMaxIntegrantes()) {
            throw new IllegalStateException("Este squad ya está completo.");
        }

        SquadSolicitud solicitud = squadSolicitudRepository
                .findBySquadIdAndUsuarioId(squadId, request.usuarioId())
                .orElse(null);

        if (solicitud != null && solicitud.getEstado() == EstadoSolicitudSquad.PENDIENTE) {
            throw new IllegalStateException("Ya existe una solicitud pendiente para este squad.");
        }

        if (solicitud == null) {
            solicitud = SquadSolicitud.builder()
                    .squad(squad)
                    .usuarioId(request.usuarioId())
                    .estado(EstadoSolicitudSquad.PENDIENTE)
                    .build();
        } else {
            solicitud.setEstado(EstadoSolicitudSquad.PENDIENTE);
            solicitud.setFechaRespuesta(null);
        }

        solicitud = squadSolicitudRepository.save(solicitud);

        publicarSolicitudSquadCreada(solicitud);

        return toSolicitudResponse(solicitud);
    }

    @Transactional(readOnly = true)
    public List<SolicitudSquadResponse> listarSolicitudesPendientes(Long squadId, Long adminId) {
        validarAdminSquad(squadId, adminId);

        return squadSolicitudRepository
                .findBySquadIdAndEstadoOrderByFechaSolicitudDesc(
                        squadId,
                        EstadoSolicitudSquad.PENDIENTE
                )
                .stream()
                .map(this::toSolicitudResponse)
                .toList();
    }

    @Transactional
    public SolicitudSquadResponse aceptarSolicitud(Long solicitudId, Long adminId) {
        SquadSolicitud solicitud = squadSolicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la solicitud: " + solicitudId));

        Squad squad = solicitud.getSquad();
        validarAdminSquad(squad.getId(), adminId);

        if (solicitud.getEstado() != EstadoSolicitudSquad.PENDIENTE) {
            throw new IllegalStateException("La solicitud ya fue respondida.");
        }

        if (squadMiembroRepository.countBySquadId(squad.getId()) >= squad.getMaxIntegrantes()) {
            throw new IllegalStateException("Este squad ya está completo.");
        }

        if (!squadMiembroRepository.existsBySquadIdAndUsuarioId(squad.getId(), solicitud.getUsuarioId())) {
            SquadMiembro miembro = SquadMiembro.builder()
                    .squad(squad)
                    .usuarioId(solicitud.getUsuarioId())
                    .rol(RolSquad.MIEMBRO)
                    .build();

            squadMiembroRepository.save(miembro);
        }

        solicitud.setEstado(EstadoSolicitudSquad.ACEPTADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());

        solicitud = squadSolicitudRepository.save(solicitud);

        publicarSolicitudSquadAceptada(solicitud, adminId);

        return toSolicitudResponse(solicitud);
    }

    @Transactional
    public SolicitudSquadResponse rechazarSolicitud(Long solicitudId, Long adminId) {
        SquadSolicitud solicitud = squadSolicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la solicitud: " + solicitudId));

        validarAdminSquad(solicitud.getSquad().getId(), adminId);

        if (solicitud.getEstado() != EstadoSolicitudSquad.PENDIENTE) {
            throw new IllegalStateException("La solicitud ya fue respondida.");
        }

        solicitud.setEstado(EstadoSolicitudSquad.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());

        return toSolicitudResponse(squadSolicitudRepository.save(solicitud));
    }

    @Transactional
    public MensajeSquadResponse enviarMensaje(Long squadId, EnviarMensajeSquadRequest request) {
        Squad squad = obtenerSquadActivo(squadId);

        if (!squadMiembroRepository.existsBySquadIdAndUsuarioId(squadId, request.remitenteId())) {
            throw new IllegalStateException("No puedes enviar mensajes porque no eres miembro de este squad.");
        }

        MensajeSquad mensaje = MensajeSquad.builder()
                .squad(squad)
                .remitenteId(request.remitenteId())
                .contenido(normalizarTexto(request.contenido()))
                .build();

        mensaje = mensajeSquadRepository.save(mensaje);

        return toMensajeResponse(mensaje);
    }

    @Transactional
    public void expulsarMiembro(Long squadId, Long adminId, Long usuarioId) {
        Squad squad = obtenerSquadActivo(squadId);

        validarAdminSquad(squadId, adminId);

        if (adminId.equals(usuarioId)) {
            throw new IllegalStateException("No puedes expulsarte a ti mismo del squad.");
        }

        if (squad.getCreadorId().equals(usuarioId)) {
            throw new IllegalStateException("No puedes expulsar al creador del squad.");
        }

        SquadMiembro miembro = squadMiembroRepository.findBySquadIdAndUsuarioId(
                squadId,
                usuarioId
        ).orElseThrow(() -> new IllegalArgumentException("El usuario no pertenece a este squad."));

        if (miembro.getRol() == RolSquad.ADMIN) {
            throw new IllegalStateException("No puedes expulsar a otro administrador.");
        }

        squadMiembroRepository.delete(miembro);

        log.info(
                "Miembro expulsado del squad. squadId={}, adminId={}, usuarioId={}",
                squadId,
                adminId,
                usuarioId
        );
    }

    @Transactional(readOnly = true)
    public Page<MensajeSquadResponse> obtenerMensajes(Long squadId, Long usuarioId, int page, int size) {
        asegurarSquadExiste(squadId);

        if (!squadMiembroRepository.existsBySquadIdAndUsuarioId(squadId, usuarioId)) {
            throw new IllegalStateException("No puedes ver mensajes porque no eres miembro de este squad.");
        }

        return mensajeSquadRepository
                .findBySquadIdOrderByFechaEnvioDesc(squadId, PageRequest.of(page, size))
                .map(this::toMensajeResponse);
    }

    private void publicarSolicitudSquadCreada(SquadSolicitud solicitud) {
        try {
            Squad squad = solicitud.getSquad();

            SquadSolicitudCreadaEvent event = new SquadSolicitudCreadaEvent(
                    solicitud.getId(),
                    squad.getId(),
                    squad.getNombre(),
                    solicitud.getUsuarioId(),
                    squad.getCreadorId()
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_COMUNICACION,
                    RabbitMQConfig.ROUTING_KEY_SQUAD_SOLICITUD_CREADA,
                    event
            );

            log.info(
                    "SquadSolicitudCreadaEvent enviado. solicitudId={}, squadId={}, solicitanteId={}, adminId={}",
                    event.solicitudId(),
                    event.squadId(),
                    event.solicitanteId(),
                    event.adminId()
            );
        } catch (Exception e) {
            log.warn(
                    "No se pudo publicar notificación de solicitud squad. solicitudId={}, motivo={}",
                    solicitud.getId(),
                    e.getMessage()
            );
        }
    }

    private void publicarSolicitudSquadAceptada(SquadSolicitud solicitud, Long adminId) {
        try {
            Squad squad = solicitud.getSquad();

            SquadSolicitudAceptadaEvent event = new SquadSolicitudAceptadaEvent(
                    solicitud.getId(),
                    squad.getId(),
                    squad.getNombre(),
                    solicitud.getUsuarioId(),
                    adminId
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_COMUNICACION,
                    RabbitMQConfig.ROUTING_KEY_SQUAD_SOLICITUD_ACEPTADA,
                    event
            );

            log.info(
                    "SquadSolicitudAceptadaEvent enviado. solicitudId={}, squadId={}, usuarioId={}, adminId={}",
                    event.solicitudId(),
                    event.squadId(),
                    event.usuarioId(),
                    event.adminId()
            );
        } catch (Exception e) {
            log.warn(
                    "No se pudo publicar notificación de aceptación squad. solicitudId={}, motivo={}",
                    solicitud.getId(),
                    e.getMessage()
            );
        }
    }

    private Squad obtenerSquadActivo(Long squadId) {
        Squad squad = asegurarSquadExiste(squadId);

        if (squad.getEstado() != EstadoSquad.ACTIVO) {
            throw new IllegalStateException("El squad no está activo.");
        }

        return squad;
    }

    private Squad asegurarSquadExiste(Long squadId) {
        return squadRepository.findById(squadId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el squad: " + squadId));
    }

    private void validarAdminSquad(Long squadId, Long adminId) {
        SquadMiembro miembro = squadMiembroRepository.findBySquadIdAndUsuarioId(squadId, adminId)
                .orElseThrow(() -> new IllegalStateException("No eres miembro de este squad."));

        if (miembro.getRol() != RolSquad.ADMIN) {
            throw new IllegalStateException("Solo el administrador del squad puede realizar esta acción.");
        }
    }

    private SquadResponse toResponse(Squad squad, Long usuarioId, Double miLatitud, Double miLongitud) {
        int integrantes = Math.toIntExact(squadMiembroRepository.countBySquadId(squad.getId()));

        boolean esMiembro = usuarioId != null &&
                squadMiembroRepository.existsBySquadIdAndUsuarioId(squad.getId(), usuarioId);

        boolean solicitudPendiente = usuarioId != null &&
                squadSolicitudRepository.existsBySquadIdAndUsuarioIdAndEstado(
                        squad.getId(),
                        usuarioId,
                        EstadoSolicitudSquad.PENDIENTE
                );

        Double distanciaKm = calcularDistanciaKm(
                miLatitud,
                miLongitud,
                squad.getLatitud(),
                squad.getLongitud()
        );

        return new SquadResponse(
                squad.getId(),
                squad.getCreadorId(),
                squad.getNombre(),
                squad.getDeporte(),
                squad.getDescripcion(),
                squad.getMaxIntegrantes(),
                integrantes,
                squad.getLatitud(),
                squad.getLongitud(),
                distanciaKm,
                squad.getEstado(),
                squad.getFechaCreacion(),
                esMiembro,
                solicitudPendiente
        );
    }

    private SolicitudSquadResponse toSolicitudResponse(SquadSolicitud solicitud) {
        return new SolicitudSquadResponse(
                solicitud.getId(),
                solicitud.getSquad().getId(),
                solicitud.getUsuarioId(),
                solicitud.getEstado(),
                solicitud.getFechaSolicitud(),
                solicitud.getFechaRespuesta()
        );
    }

    private SquadMiembroResponse toMiembroResponse(SquadMiembro miembro) {
        return new SquadMiembroResponse(
                miembro.getId(),
                miembro.getSquad().getId(),
                miembro.getUsuarioId(),
                miembro.getRol(),
                miembro.getFechaUnion()
        );
    }

    private MensajeSquadResponse toMensajeResponse(MensajeSquad mensaje) {
        return new MensajeSquadResponse(
                mensaje.getId(),
                mensaje.getSquad().getId(),
                mensaje.getRemitenteId(),
                mensaje.getContenido(),
                mensaje.getFechaEnvio()
        );
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private boolean deporteCoincide(String deporteSquad, String deporteFiltro) {
        if (deporteFiltro == null || deporteFiltro.isBlank()) {
            return true;
        }

        if (deporteFiltro.equalsIgnoreCase("TODOS") || deporteFiltro.equalsIgnoreCase("Todos")) {
            return true;
        }

        return deporteSquad != null && deporteSquad.equalsIgnoreCase(deporteFiltro);
    }

    private double normalizarDistancia(Double distancia, double valorDefecto) {
        if (distancia == null || distancia.isNaN() || distancia.isInfinite()) {
            return valorDefecto;
        }

        if (distancia < 1) {
            return 1;
        }

        if (distancia > 500) {
            return 500;
        }

        return distancia;
    }

    private Double calcularDistanciaKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }

        final int radioTierraKm = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round((radioTierraKm * c) * 10.0) / 10.0;
    }
}