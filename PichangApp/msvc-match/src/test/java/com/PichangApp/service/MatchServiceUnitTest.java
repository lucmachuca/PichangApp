package com.PichangApp.service;

import com.PichangApp.dto.InteraccionRequest;
import com.PichangApp.dto.InteraccionResponse;
import com.PichangApp.dto.MatchSocialResponse;
import com.PichangApp.model.Interaccion;
import com.PichangApp.model.MatchSocial;
import com.PichangApp.model.enums.TipoInteraccion;
import com.PichangApp.repository.InteraccionRepository;
import com.PichangApp.repository.MatchSocialRepository;
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
class MatchServiceUnitTest {

    @Mock
    private InteraccionRepository interaccionRepository;

    @Mock
    private MatchSocialRepository matchSocialRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MatchService matchService;

    @Test
    void DebeCrearMatchCuandoAmbosUsuariosSeDanMeGusta() {
        InteraccionRequest request = new InteraccionRequest(
                11L,
                12L,
                TipoInteraccion.ME_GUSTA
        );

        Interaccion interaccionGuardada = Interaccion.builder()
                .id(100L)
                .usuarioOrigenId(11L)
                .usuarioDestinoId(12L)
                .tipo(TipoInteraccion.ME_GUSTA)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Interaccion interaccionAnterior = Interaccion.builder()
                .id(99L)
                .usuarioOrigenId(12L)
                .usuarioDestinoId(11L)
                .tipo(TipoInteraccion.ME_GUSTA)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(interaccionRepository.existsByUsuarioOrigenIdAndUsuarioDestinoId(11L, 12L))
                .thenReturn(false);

        when(interaccionRepository.save(any(Interaccion.class)))
                .thenReturn(interaccionGuardada);

        when(interaccionRepository.findByUsuarioOrigenIdAndUsuarioDestinoIdAndTipo(
                12L,
                11L,
                TipoInteraccion.ME_GUSTA
        )).thenReturn(Optional.of(interaccionAnterior));

        when(matchSocialRepository.findByUsers(11L, 12L))
                .thenReturn(Optional.empty());

        when(matchSocialRepository.save(any(MatchSocial.class)))
                .thenAnswer(invocation -> {
                    MatchSocial match = invocation.getArgument(0);
                    match.setId(50L);
                    return match;
                });

        InteraccionResponse response = matchService.registerInteraccion(request);

        assertEquals(100L, response.id());
        assertEquals(11L, response.usuarioOrigenId());
        assertEquals(12L, response.usuarioDestinoId());
        assertEquals(TipoInteraccion.ME_GUSTA, response.tipo());
        assertTrue(response.hayMatch());

        verify(interaccionRepository).save(any(Interaccion.class));
        verify(matchSocialRepository).save(any(MatchSocial.class));
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void NoDebePermitirQueUnUsuarioInteractueConsigoMismo() {
        InteraccionRequest request = new InteraccionRequest(
                11L,
                11L,
                TipoInteraccion.ME_GUSTA
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> matchService.registerInteraccion(request)
        );

        verify(interaccionRepository, never()).save(any(Interaccion.class));
        verify(matchSocialRepository, never()).save(any(MatchSocial.class));
    }

    @Test
    void NoDebePermitirUnaInteraccionDuplicada() {
        InteraccionRequest request = new InteraccionRequest(
                11L,
                12L,
                TipoInteraccion.ME_GUSTA
        );

        when(interaccionRepository.existsByUsuarioOrigenIdAndUsuarioDestinoId(11L, 12L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> matchService.registerInteraccion(request)
        );

        verify(interaccionRepository, never()).save(any(Interaccion.class));
        verify(matchSocialRepository, never()).save(any(MatchSocial.class));
    }

    @Test
    void NoDebeCrearMatchCuandoLaInteraccionEsNoMegusta() {
        InteraccionRequest request = new InteraccionRequest(
                11L,
                12L,
                TipoInteraccion.DISME_GUSTA
        );

        Interaccion interaccionGuardada = Interaccion.builder()
                .id(101L)
                .usuarioOrigenId(11L)
                .usuarioDestinoId(12L)
                .tipo(TipoInteraccion.DISME_GUSTA)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(interaccionRepository.existsByUsuarioOrigenIdAndUsuarioDestinoId(11L, 12L))
                .thenReturn(false);

        when(interaccionRepository.save(any(Interaccion.class)))
                .thenReturn(interaccionGuardada);

        InteraccionResponse response = matchService.registerInteraccion(request);

        assertEquals(101L, response.id());
        assertEquals(TipoInteraccion.DISME_GUSTA, response.tipo());
        assertFalse(response.hayMatch());

        verify(interaccionRepository).save(any(Interaccion.class));
        verify(matchSocialRepository, never()).save(any(MatchSocial.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void DebeListarLosMatchesActivosDeUnUsuario() {
        MatchSocial match = MatchSocial.builder()
                .id(50L)
                .usuarioAId(11L)
                .usuarioBId(12L)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(matchSocialRepository.findActiveMatchesByUserId(11L))
                .thenReturn(List.of(match));

        List<MatchSocialResponse> matches = matchService.obtenerMatchesPorUsuario(11L);

        assertEquals(1, matches.size());
        assertEquals(50L, matches.get(0).id());
        assertEquals(11L, matches.get(0).usuarioAId());
        assertEquals(12L, matches.get(0).usuarioBId());
        assertTrue(matches.get(0).activo());
    }

    @Test
    void NoDebeBuscarUnMatchInexistente() {
        when(matchSocialRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> matchService.obtenerMatchPorId(999L)
        );

        verify(matchSocialRepository).findById(999L);
    }
}