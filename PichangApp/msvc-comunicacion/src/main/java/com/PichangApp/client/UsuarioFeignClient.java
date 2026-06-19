package com.PichangApp.client;

import com.PichangApp.dto.UsuarioBasicoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "msvc-usuario-comunicacion",
        url = "${msvc.usuario.url:http://localhost:8001}",
        path = "/api/users/internal"
)
public interface UsuarioFeignClient {

    @GetMapping("/{id}")
    UsuarioBasicoDTO obtenerUsuarioBasico(@PathVariable("id") Long id);
}
