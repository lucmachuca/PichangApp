package com.PichangApp.msvc_notificacion;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Cliente Feign ultra-minimalista para consultar el email de un usuario
@FeignClient(name = "msvc-usuario", url = "${msvc.usuario.url:http://localhost:8001}", path = "/api/v1/usuarios")
public interface UsuarioFeignClient {

    @GetMapping("/{id}")
    UsuarioBasicoDto obtenerUsuarioBasico(@PathVariable("id") Long id);

    // Record interno para no crear archivos DTO extra (lo justo y necesario)
    record UsuarioBasicoDto(Long id, String email, String nombre) {}
}
