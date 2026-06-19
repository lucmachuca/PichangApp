package com.PichangApp.msvc_notificacion.service;

import com.PichangApp.msvc_notificacion.dto.CrearNotificacionRequest;
import com.PichangApp.msvc_notificacion.dto.NotificacionResponse;
import com.PichangApp.msvc_notificacion.model.Notificacion;
import com.PichangApp.msvc_notificacion.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Transactional
    public NotificacionResponse crearNotificacion(CrearNotificacionRequest request) {
        Notificacion notificacion = Notificacion.builder()
                .usuarioId(request.usuarioId())
                .titulo(request.titulo())
                .mensaje(request.mensaje())
                .tipo(request.tipo())
                .leida(false)
                .build();

        return toResponse(notificacionRepository.save(notificacion));
    }

    @Transactional
    public NotificacionResponse crearNotificacion(
            Long usuarioId,
            String titulo,
            String mensaje,
            String tipo
    ) {
        return crearNotificacion(
                new CrearNotificacionRequest(usuarioId, titulo, mensaje, tipo)
        );
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificacionResponse marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la notificación con id " + id));

        notificacion.setLeida(true);

        return toResponse(notificacionRepository.save(notificacion));
    }

    private NotificacionResponse toResponse(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getUsuarioId(),
                notificacion.getTitulo(),
                notificacion.getMensaje(),
                notificacion.getTipo(),
                notificacion.getLeida(),
                notificacion.getFechaCreacion()
        );
    }
}