package com.PichangApp.msvc_seguridad.services;

import com.PichangApp.msvc_seguridad.dtos.BloqueoRequest;
import com.PichangApp.msvc_seguridad.dtos.BloqueoResponse;
import com.PichangApp.msvc_seguridad.models.Bloqueo;
import com.PichangApp.msvc_seguridad.repositories.BloqueoRepository;
import com.PichangApp.msvc_seguridad.repositories.ReporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeguridadSafetyServiceUnitTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private BloqueoRepository bloqueoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SeguridadSafetyService seguridadSafetyService;

    @Test
    void bloquearUsuarioGuardaBloqueoYPublicaEventoLocal() {
        BloqueoRequest request = new BloqueoRequest(11L, 12L);

        Bloqueo bloqueoGuardado = Bloqueo.builder()
                .idBloqueo(30L)
                .idUsuarioOrigen(11L)
                .idUsuarioBloqueado(12L)
                .fechaBloqueo(LocalDateTime.now())
                .build();

        when(bloqueoRepository.findByIdUsuarioOrigenAndIdUsuarioBloqueado(11L, 12L))
                .thenReturn(Optional.empty());

        when(bloqueoRepository.save(any(Bloqueo.class)))
                .thenReturn(bloqueoGuardado);

        BloqueoResponse response = seguridadSafetyService.bloquearUsuario(request);

        assertEquals(30L, response.idBloqueo());
        assertEquals(11L, response.idUsuarioOrigen());
        assertEquals(12L, response.idUsuarioBloqueado());
        assertNotNull(response.fechaBloqueo());

        verify(bloqueoRepository).findByIdUsuarioOrigenAndIdUsuarioBloqueado(11L, 12L);
        verify(bloqueoRepository).save(any(Bloqueo.class));
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void desbloquearUsuarioEliminaBloqueoSiExiste() {
        BloqueoRequest request = new BloqueoRequest(11L, 12L);

        Bloqueo bloqueo = Bloqueo.builder()
                .idBloqueo(30L)
                .idUsuarioOrigen(11L)
                .idUsuarioBloqueado(12L)
                .fechaBloqueo(LocalDateTime.now())
                .build();

        when(bloqueoRepository.findByIdUsuarioOrigenAndIdUsuarioBloqueado(11L, 12L))
                .thenReturn(Optional.of(bloqueo));

        seguridadSafetyService.desbloquearUsuario(request);

        verify(bloqueoRepository).findByIdUsuarioOrigenAndIdUsuarioBloqueado(11L, 12L);
        verify(bloqueoRepository).delete(bloqueo);
        verify(eventPublisher).publishEvent(any(Object.class));
    }
}