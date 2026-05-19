package com.PichangApp.service;

import com.PichangApp.dto.MensajeRequest;
import com.PichangApp.dto.MensajeResponse;
import com.PichangApp.exception.ResourceNotFoundException;
import com.PichangApp.feign.MatchFeignClient;
import com.PichangApp.model.Mensaje;
import com.PichangApp.repository.MensajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para la gestión de mensajes.  Encapsula la
 * validación del match social a través de un cliente Feign y mapea
 * entre las entidades JPA y los DTOs de entrada/salida.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final MatchFeignClient matchFeignClient;

    /**
     * Envía un mensaje validando previamente que el usuario emisor pertenece
     * al MatchSocial indicado.  Se delega la validación a msvc-match a
     * través del cliente Feign.  En caso de que el match no exista o el
     * usuario no participe en él, se lanza una excepción.
     *
     * @param request los datos del mensaje a enviar
     * @return el mensaje persistido como DTO de respuesta
     */
    @Transactional
    public MensajeResponse enviarMensaje(MensajeRequest request) {
        // Consultar el match social y validar que el usuario pertenezca al mismo
        var match = matchFeignClient.obtenerMatch(request.idMatch());
        if (match == null) {
            throw new ResourceNotFoundException("No se encontró el Match con el ID: " + request.idMatch());
        }
        // Validar que el usuario emisor sea uno de los participantes
        boolean pertenece = match.usuarioAId().equals(request.idEmisor()) || match.usuarioBId().equals(request.idEmisor());
        if (!pertenece) {
            throw new IllegalArgumentException("El usuario " + request.idEmisor() + " no pertenece al Match indicado.");
        }

        Mensaje mensaje = Mensaje.builder()
                .idMatch(request.idMatch())
                .idEmisor(request.idEmisor())
                .contenido(request.contenido())
                .leido(Boolean.FALSE)
                .build();
        mensaje = mensajeRepository.save(mensaje);
        return toDto(mensaje);
    }

    /**
     * Obtiene la lista de mensajes asociados a un match social ordenados por
     * fecha de envío ascendente.  Si no se encuentran mensajes, se devuelve
     * una lista vacía.
     *
     * @param idMatch identificador del MatchSocial
     * @return lista de mensajes como DTOs
     */
    public List<MensajeResponse> listarMensajes(UUID idMatch) {
        return mensajeRepository.findByIdMatchOrderByFechaEnvioAsc(idMatch)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private MensajeResponse toDto(Mensaje mensaje) {
        return new MensajeResponse(
                mensaje.getIdMensaje(),
                mensaje.getIdMatch(),
                mensaje.getIdEmisor(),
                mensaje.getContenido(),
                mensaje.getFechaEnvio(),
                mensaje.getLeido()
        );
    }
}