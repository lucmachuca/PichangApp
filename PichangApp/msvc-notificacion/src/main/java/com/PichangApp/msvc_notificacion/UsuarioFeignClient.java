package com.PichangApp.msvc_notificacion;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "msvc-usuario",
        url = "${msvc.usuario.url:http://localhost:8001}",
        path = "/api/users"
)
public interface UsuarioFeignClient {

    @GetMapping("/{id}")
    UsuarioBasicoDto obtenerUsuarioBasico(@PathVariable("id") Long id);

    record UsuarioBasicoDto(Long id, String email, String nombre) {
    }
}