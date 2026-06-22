package com.PichangApp.msvc_seguridad.services;

import com.PichangApp.msvc_seguridad.dtos.BloqueoRequest;
import com.PichangApp.msvc_seguridad.dtos.BloqueoResponse;
import com.PichangApp.msvc_seguridad.dtos.ReporteRequest;
import com.PichangApp.msvc_seguridad.dtos.ReporteResponse;
import com.PichangApp.msvc_seguridad.models.Bloqueo;
import com.PichangApp.msvc_seguridad.models.Reporte;
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
import java.util.List;
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
    void GuardarUnReportePendiente() {
        ReporteRequest request = new ReporteRequest(
                11L,
                12L,
                "Conducta inapropiada",
                "Usuario reportado durante prueba EP3"
        );

        Reporte reporteGuardado = Reporte.builder()
                .idReporte(40L)
                .idUsuarioDenunciante(11L)
                .idUsuarioDenunciado(12L)
                .motivo("Conducta inapropiada")
                .descripcion("Usuario reportado durante prueba EP3")
                .estado("PENDIENTE")
                .fechaReporte(LocalDateTime.now())
                .build();

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteGuardado);

        ReporteResponse response = seguridadSafetyService.reportarUsuario(request);

        assertEquals(40L, response.idReporte());
        assertEquals(11L, response.idUsuarioDenunciante());
        assertEquals(12L, response.idUsuarioDenunciado());
        assertEquals("Conducta inapropiada", response.motivo());
        assertEquals("PENDIENTE", response.estado());

        verify(reporteRepository).save(any(Reporte.class));
    }


    @Test
    void BloqueaUnUsuario() {
        BloqueoRequest request = new BloqueoRequest(
                11L,
                12L
        );

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

        verify(bloqueoRepository).save(any(Bloqueo.class));
        verify(eventPublisher).publishEvent(any(Object.class));
    }


    @Test
    void EliminaBloqueoAlDesbloquearUsuario() {
        BloqueoRequest request = new BloqueoRequest(
                11L,
                12L
        );

        Bloqueo bloqueo = Bloqueo.builder()
                .idBloqueo(30L)
                .idUsuarioOrigen(11L)
                .idUsuarioBloqueado(12L)
                .fechaBloqueo(LocalDateTime.now())
                .build();

        when(bloqueoRepository.findByIdUsuarioOrigenAndIdUsuarioBloqueado(11L, 12L))
                .thenReturn(Optional.of(bloqueo));

        seguridadSafetyService.desbloquearUsuario(request);

        verify(bloqueoRepository).delete(bloqueo);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void DetectaBloqueoEntreUsuarioAyB() {
        Bloqueo bloqueo = Bloqueo.builder()
                .idBloqueo(30L)
                .idUsuarioOrigen(11L)
                .idUsuarioBloqueado(12L)
                .fechaBloqueo(LocalDateTime.now())
                .build();

        when(bloqueoRepository.findByIdUsuarioOrigenAndIdUsuarioBloqueado(11L, 12L))
                .thenReturn(Optional.of(bloqueo));

        boolean existeBloqueo = seguridadSafetyService.existeBloqueoEntreUsuarios(11L, 12L);

        assertTrue(existeBloqueo);
    }

    @Test
    void DetectaBloqueoEntreUsuarioByA() {
        Bloqueo bloqueo = Bloqueo.builder()
                .idBloqueo(31L)
                .idUsuarioOrigen(12L)
                .idUsuarioBloqueado(11L)
                .fechaBloqueo(LocalDateTime.now())
                .build();

        when(bloqueoRepository.findByIdUsuarioOrigenAndIdUsuarioBloqueado(11L, 12L))
                .thenReturn(Optional.empty());

        when(bloqueoRepository.findByIdUsuarioOrigenAndIdUsuarioBloqueado(12L, 11L))
                .thenReturn(Optional.of(bloqueo));

        boolean existeBloqueo = seguridadSafetyService.existeBloqueoEntreUsuarios(11L, 12L);

        assertTrue(existeBloqueo);
    }

    @Test
    void RetornaFalseCuandoNoExisteBloqueoEntreUsuarios() {
        when(bloqueoRepository.findByIdUsuarioOrigenAndIdUsuarioBloqueado(11L, 12L))
                .thenReturn(Optional.empty());

        when(bloqueoRepository.findByIdUsuarioOrigenAndIdUsuarioBloqueado(12L, 11L))
                .thenReturn(Optional.empty());

        boolean existeBloqueo = seguridadSafetyService.existeBloqueoEntreUsuarios(11L, 12L);

        assertFalse(existeBloqueo);
    }

    @Test
    void DebeListarBloqueosDeUnUsuario() {
        Bloqueo bloqueo = Bloqueo.builder()
                .idBloqueo(30L)
                .idUsuarioOrigen(11L)
                .idUsuarioBloqueado(12L)
                .fechaBloqueo(LocalDateTime.now())
                .build();

        when(bloqueoRepository.findByIdUsuarioOrigen(11L))
                .thenReturn(List.of(bloqueo));

        List<BloqueoResponse> bloqueos = seguridadSafetyService.listarBloqueosPorUsuario(11L);

        assertEquals(1, bloqueos.size());
        assertEquals(30L, bloqueos.get(0).idBloqueo());
        assertEquals(11L, bloqueos.get(0).idUsuarioOrigen());
        assertEquals(12L, bloqueos.get(0).idUsuarioBloqueado());
    }
}