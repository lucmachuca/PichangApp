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
import java.util.List;
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
    void CreaNotificacion() {
        CrearNotificacionRequest request = new CrearNotificacionRequest(
                11L,
                "Nuevo match",
                "Tienes un nuevo match deportivo",
                "MATCH"
        );

        Notificacion notificacionGuardada = Notificacion.builder()
                .id(80L)
                .usuarioId(11L)
                .titulo("Nuevo match")
                .mensaje("Tienes un nuevo match deportivo")
                .tipo("MATCH")
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenReturn(notificacionGuardada);

        NotificacionResponse response = notificacionService.crearNotificacion(request);

        assertEquals(80L, response.id());
        assertEquals(11L, response.usuarioId());
        assertEquals("Nuevo match", response.titulo());
        assertEquals("MATCH", response.tipo());
        assertFalse(response.leida());

        verify(notificacionRepository).save(any(Notificacion.class));
    }

    @Test
    void ListarNotificacionesDeUnUsuario() {
        Notificacion notificacion1 = Notificacion.builder()
                .id(80L)
                .usuarioId(11L)
                .titulo("Nuevo match")
                .mensaje("Tienes un nuevo match")
                .tipo("MATCH")
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Notificacion notificacion2 = Notificacion.builder()
                .id(81L)
                .usuarioId(11L)
                .titulo("Nuevo mensaje")
                .mensaje("Tienes un nuevo mensaje")
                .tipo("MENSAJE")
                .leida(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(11L))
                .thenReturn(List.of(notificacion1, notificacion2));

        List<NotificacionResponse> resultado = notificacionService.listarPorUsuario(11L);

        assertEquals(2, resultado.size());

        assertEquals("Nuevo match", resultado.get(0).titulo());
        assertFalse(resultado.get(0).leida());

        assertEquals("Nuevo mensaje", resultado.get(1).titulo());
        assertTrue(resultado.get(1).leida());

        verify(notificacionRepository).findByUsuarioIdOrderByFechaCreacionDesc(11L);
    }

    @Test
    void MarcarNotificacionComoLeida() {
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

    @Test
    void marcarComoLeidaInexistenteLanzaExcepcion() {
        when(notificacionRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificacionService.marcarComoLeida(999L);
        });

        verify(notificacionRepository).findById(999L);
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }
}
