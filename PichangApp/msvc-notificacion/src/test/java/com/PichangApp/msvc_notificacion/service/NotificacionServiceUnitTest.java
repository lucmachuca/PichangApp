package com.PichangApp.msvc_notificacion.service;

import com.PichangApp.msvc_notificacion.dto.CrearNotificacionRequest;
import com.PichangApp.msvc_notificacion.dto.NotificacionResponse;
import com.PichangApp.msvc_notificacion.model.Notificacion;
import com.PichangApp.msvc_notificacion.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceUnitTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    @Test
    void crearNotificacionLaGuardaComoNoLeida() {
        CrearNotificacionRequest request = new CrearNotificacionRequest(
                11L,
                "Nuevo match",
                "Tienes un nuevo match deportivo",
                "MATCH"
        );

        Notificacion guardada = Notificacion.builder()
                .id(80L)
                .usuarioId(11L)
                .titulo("Nuevo match")
                .mensaje("Tienes un nuevo match deportivo")
                .tipo("MATCH")
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenReturn(guardada);

        NotificacionResponse response = notificacionService.crearNotificacion(request);

        assertEquals(80L, response.id());
        assertEquals(11L, response.usuarioId());
        assertEquals("Nuevo match", response.titulo());
        assertEquals("MATCH", response.tipo());
        assertFalse(response.leida());

        verify(notificacionRepository).save(any(Notificacion.class));
    }

    @Test
    void marcarComoLeidaCambiaEstadoDeNotificacion() {
        Notificacion notificacion = Notificacion.builder()
                .id(80L)
                .usuarioId(11L)
                .titulo("Mensaje")
                .mensaje("Tienes un mensaje nuevo")
                .tipo("MENSAJE")
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(notificacionRepository.findById(80L))
                .thenReturn(Optional.of(notificacion));

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificacionResponse response = notificacionService.marcarComoLeida(80L);

        assertTrue(response.leida());

        verify(notificacionRepository).findById(80L);
        verify(notificacionRepository).save(notificacion);
    }
}
