package com.PichangApp.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Cliente Feign utilizado para comunicarse con el microservicio de match.
 * Se consulta para validar que el MatchSocial existe y obtener sus datos.
 *
 * La URL base y el nombre del servicio pueden configurarse mediante
 * propiedades de Spring Cloud (p. ej. usando eureka).  Para esta
 * auditoría se define directamente el nombre del servicio para que
 * Feign lo resuelva si se configura un load balancer.
 */
@FeignClient(
        name = "msvc-match",
        // Proporcionamos la URL base a través de una propiedad para permitir
        // la comunicación directa sin necesidad de un servidor de
        // descubrimiento.  Si no se define msvc.match.url, Feign seguirá
        // intentando resolver el nombre mediante un load balancer.
        url = "${msvc.match.url:http://localhost:8081}",
        path = "/api/v1"
)
public interface MatchFeignClient {

    /**
     * Obtiene los datos de un MatchSocial por su identificador.  El
     * endpoint es proporcionado por msvc-match y debería devolver un
     * objeto con los participantes del match.  Se utiliza UUID para
     * mantener la consistencia con la arquitectura sugerida; sin embargo,
     * actualmente msvc-match emplea identificadores Long, por lo que se
     * recomienda al equipo de desarrollo actualizar dicho microservicio.
     *
     * @param matchId identificador del match
     * @return una representación del match
     */
    @GetMapping("/matches/{matchId}")
    MatchDto obtenerMatch(@PathVariable("matchId") UUID matchId);

    /**
     * DTO anidado que representa la respuesta del servicio de matches.
     * Se definen únicamente los campos relevantes para validar un
     * mensaje: los identificadores de los usuarios A y B.  Otros
     * campos del match quedan fuera del alcance de este cliente.
     *
     * Al mantener el DTO dentro del cliente se aísla la dependencia
     * del esquema de datos del microservicio externo.
     */
    record MatchDto(UUID id, UUID usuarioAId, UUID usuarioBId, boolean activo) {}
}