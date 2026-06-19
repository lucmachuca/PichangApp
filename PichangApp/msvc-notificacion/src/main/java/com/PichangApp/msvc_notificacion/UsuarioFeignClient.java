package com.PichangApp.msvc_notificacion;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "msvc-usuario-notificacion",
        url = "${msvc.usuario.url:http://localhost:8001}",
        path = "/api/users/internal"
)
public interface UsuarioFeignClient {

    @GetMapping("/{id}")
    UsuarioBasicoDto obtenerUsuarioBasico(@PathVariable("id") Long id);

    record UsuarioBasicoDto(
            Long id,
            String username,
            String nombre,
            String apellido,
            String email
    ) {
        public String nombreCompleto() {
            String nombreSeguro = nombre != null ? nombre : "";
            String apellidoSeguro = apellido != null ? apellido : "";
            String completo = (nombreSeguro + " " + apellidoSeguro).trim();

            if (!completo.isBlank()) {
                return completo;
            }

            if (username != null && !username.isBlank()) {
                return username;
            }

            return "Usuario " + id;
        }
    }
}