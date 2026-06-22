package com.PichangApp.service;

import com.PichangApp.client.MatchFeignClient;
import com.PichangApp.client.UsuarioFeignClient;
import com.PichangApp.config.RabbitMQConfig;
import com.PichangApp.dto.CrearSalaRequest;
import com.PichangApp.dto.EnviarMensajeRequest;
import com.PichangApp.dto.MatchSocialDTO;
import com.PichangApp.dto.MensajeChatResponse;
import com.PichangApp.dto.MensajeCreadoEvent;
import com.PichangApp.dto.SalaChatResponse;
import com.PichangApp.dto.UsuarioBasicoDTO;
import com.PichangApp.model.MensajeChat;
import com.PichangApp.model.SalaChat;
import com.PichangApp.model.enums.EstadoSala;
import com.PichangApp.model.enums.TipoMensaje;
import com.PichangApp.repository.BloqueoUsuarioRepository;
import com.PichangApp.repository.MensajeChatRepository;
import com.PichangApp.repository.SalaChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaChatServiceUnitTest {

    @Mock
    private SalaChatRepository salaChatRepository;

    @Mock
    private MensajeChatRepository mensajeChatRepository;

    @Mock
    private BloqueoUsuarioRepository bloqueoUsuarioRepository;

    @Mock
    private UsuarioFeignClient usuarioFeignClient;

    @Mock
    private MatchFeignClient matchFeignClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SalaChatService salaChatService;

    @Test
    void DebeCrearSalaCuandoElMatchEsValido() {
        CrearSalaRequest request = new CrearSalaRequest(
                8L,
                11L,
                12L
        );

        MatchSocialDTO match = new MatchSocialDTO(
                8L,
                11L,
                12L,
                true,
                LocalDateTime.now()
        );

        SalaChat salaGuardada = SalaChat.builder()
                .id(7L)
                .matchSocialId(8L)
                .usuarioAId(11L)
                .usuarioBId(12L)
                .estado(EstadoSala.ACTIVA)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(matchFeignClient.obtenerMatchPorId(8L))
                .thenReturn(match);

        when(salaChatRepository.findByMatchSocialId(8L))
                .thenReturn(Optional.empty());

        when(salaChatRepository.save(any(SalaChat.class)))
                .thenReturn(salaGuardada);

        when(usuarioFeignClient.obtenerUsuarioBasico(11L))
                .thenReturn(usuarioA());

        when(usuarioFeignClient.obtenerUsuarioBasico(12L))
                .thenReturn(usuarioB());

        SalaChatResponse response = salaChatService.crearSala(request);

        assertEquals(7L, response.id());
        assertEquals(8L, response.matchSocialId());
        assertEquals(11L, response.usuarioAId());
        assertEquals(12L, response.usuarioBId());
        assertEquals("Usuario PruebaA", response.usuarioANombre());
        assertEquals("Usuario PruebaB", response.usuarioBNombre());
        assertEquals(EstadoSala.ACTIVA, response.estado());

        verify(salaChatRepository).save(any(SalaChat.class));
    }

    @Test
    void NoDebeCrearDosSalasParaElMismoMatch() {
        CrearSalaRequest request = new CrearSalaRequest(
                8L,
                11L,
                12L
        );

        MatchSocialDTO match = new MatchSocialDTO(
                8L,
                11L,
                12L,
                true,
                LocalDateTime.now()
        );

        SalaChat salaExistente = SalaChat.builder()
                .id(7L)
                .matchSocialId(8L)
                .usuarioAId(11L)
                .usuarioBId(12L)
                .estado(EstadoSala.ACTIVA)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(matchFeignClient.obtenerMatchPorId(8L))
                .thenReturn(match);

        when(salaChatRepository.findByMatchSocialId(8L))
                .thenReturn(Optional.of(salaExistente));

        assertThrows(
                IllegalStateException.class,
                () -> salaChatService.crearSala(request)
        );

        verify(salaChatRepository, never()).save(any(SalaChat.class));
    }

    @Test
    void DebeEnviarMensajeCuandoLaSalaEstaActivaYNoHayBloqueo() {
        SalaChat sala = salaActiva();

        EnviarMensajeRequest request = new EnviarMensajeRequest(
                11L,
                "Mensaje de prueba EP3",
                TipoMensaje.TEXTO,
                null
        );

        MatchSocialDTO match = new MatchSocialDTO(
                8L,
                11L,
                12L,
                true,
                LocalDateTime.now()
        );

        when(salaChatRepository.findById(7L))
                .thenReturn(Optional.of(sala));

        when(matchFeignClient.obtenerMatchPorId(8L))
                .thenReturn(match);

        when(bloqueoUsuarioRepository.existsByIdUsuarioOrigenAndIdUsuarioBloqueado(11L, 12L))
                .thenReturn(false);

        when(bloqueoUsuarioRepository.existsByIdUsuarioOrigenAndIdUsuarioBloqueado(12L, 11L))
                .thenReturn(false);

        when(usuarioFeignClient.obtenerUsuarioBasico(11L))
                .thenReturn(usuarioA());

        when(mensajeChatRepository.save(any(MensajeChat.class)))
                .thenAnswer(invocation -> {
                    MensajeChat mensaje = invocation.getArgument(0);
                    mensaje.setId(200L);
                    mensaje.setFechaEnvio(LocalDateTime.now());
                    return mensaje;
                });

        MensajeChatResponse response = salaChatService.enviarMensaje(7L, request);

        assertEquals(200L, response.id());
        assertEquals(7L, response.salaId());
        assertEquals(11L, response.remitenteId());
        assertEquals("Usuario PruebaA", response.remitenteNombre());
        assertEquals("ep3_user_a", response.remitenteUsername());
        assertEquals("Mensaje de prueba EP3", response.contenido());

        verify(mensajeChatRepository).save(any(MensajeChat.class));
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_COMUNICACION),
                eq(RabbitMQConfig.ROUTING_KEY_MENSAJE_CREADO),
                any(MensajeCreadoEvent.class)
        );
    }

    @Test
    void NoDebeEnviarMensajeSiExisteBloqueoEntreUsuarios() {
        SalaChat sala = salaActiva();

        MatchSocialDTO match = new MatchSocialDTO(
                8L,
                11L,
                12L,
                true,
                LocalDateTime.now()
        );

        EnviarMensajeRequest request = new EnviarMensajeRequest(
                11L,
                "Hola",
                TipoMensaje.TEXTO,
                null
        );

        when(salaChatRepository.findById(7L))
                .thenReturn(Optional.of(sala));

        when(matchFeignClient.obtenerMatchPorId(8L))
                .thenReturn(match);

        when(bloqueoUsuarioRepository.existsByIdUsuarioOrigenAndIdUsuarioBloqueado(11L, 12L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> salaChatService.enviarMensaje(7L, request)
        );

        verify(mensajeChatRepository, never()).save(any(MensajeChat.class));
    }

    @Test
    void DebeListarMensajesDeUnaSala() {
        SalaChat sala = salaActiva();

        MensajeChat mensaje = MensajeChat.builder()
                .id(200L)
                .salaChat(sala)
                .remitenteId(11L)
                .contenido("Mensaje listado en prueba EP3")
                .tipoMensaje(TipoMensaje.TEXTO)
                .mediaUrl(null)
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(mensajeChatRepository.findBySalaChatIdOrderByFechaEnvioDesc(
                eq(7L),
                any()
        )).thenReturn(new PageImpl<>(List.of(mensaje)));

        when(usuarioFeignClient.obtenerUsuarioBasico(11L))
                .thenReturn(usuarioA());

        var response = salaChatService.obtenerMensajes(7L, 0, 10);

        assertEquals(1, response.getTotalElements());

        MensajeChatResponse primerMensaje = response.getContent().get(0);

        assertEquals(200L, primerMensaje.id());
        assertEquals(7L, primerMensaje.salaId());
        assertEquals(11L, primerMensaje.remitenteId());
        assertEquals("Usuario PruebaA", primerMensaje.remitenteNombre());
        assertEquals("Mensaje listado en prueba EP3", primerMensaje.contenido());
    }

    private SalaChat salaActiva() {
        return SalaChat.builder()
                .id(7L)
                .matchSocialId(8L)
                .usuarioAId(11L)
                .usuarioBId(12L)
                .estado(EstadoSala.ACTIVA)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    private UsuarioBasicoDTO usuarioA() {
        return new UsuarioBasicoDTO(
                11L,
                "ep3_user_a",
                "Usuario",
                "PruebaA",
                "ep3_user_a@pichangapp.cl"
        );
    }

    private UsuarioBasicoDTO usuarioB() {
        return new UsuarioBasicoDTO(
                12L,
                "ep3_user_b",
                "Usuario",
                "PruebaB",
                "ep3_user_b@pichangapp.cl"
        );
    }
}