package com.PichangApp.service;

import com.PichangApp.client.MatchFeignClient;
import com.PichangApp.client.UsuarioFeignClient;
import com.PichangApp.config.RabbitMQConfig;
import com.PichangApp.dto.EnviarMensajeRequest;
import com.PichangApp.dto.MatchSocialDTO;
import com.PichangApp.dto.MensajeChatResponse;
import com.PichangApp.dto.MensajeCreadoEvent;
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

import java.time.LocalDateTime;
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
    void enviarMensajeValidoGuardaMensajeYPublicaEvento() {
        SalaChat sala = SalaChat.builder()
                .id(7L)
                .matchSocialId(8L)
                .usuarioAId(11L)
                .usuarioBId(12L)
                .estado(EstadoSala.ACTIVA)
                .fechaCreacion(LocalDateTime.now())
                .build();

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

        UsuarioBasicoDTO usuarioRemitente = new UsuarioBasicoDTO(
                11L,
                "ep3_user_a",
                "Usuario",
                "PruebaA",
                "ep3_user_a@pichangapp.cl"
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
                .thenReturn(usuarioRemitente);

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
        assertEquals(TipoMensaje.TEXTO, response.tipoMensaje());

        verify(mensajeChatRepository).save(any(MensajeChat.class));
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_COMUNICACION),
                eq(RabbitMQConfig.ROUTING_KEY_MENSAJE_CREADO),
                any(MensajeCreadoEvent.class)
        );
    }
}