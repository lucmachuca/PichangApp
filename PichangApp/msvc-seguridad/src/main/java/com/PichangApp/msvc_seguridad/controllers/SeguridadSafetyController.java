package com.PichangApp.msvc_seguridad.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/safety")
public class SeguridadSafetyController {

    // Simula recibir un reporte desde la app móvil
    @PostMapping("/reportar")
    public String reportarUsuario() {
        // A futuro aquí tomaremos los datos y los guardaremos en la base de datos
        return "Reporte recibido. Nuestro equipo de moderación revisará la conducta del deportista.";
    }

    // Simula bloquear a alguien
    @PostMapping("/bloquear")
    public String bloquearUsuario() {
        // A futuro guardaremos el bloqueo en la tabla
        return "Usuario bloqueado exitosamente. Ya no podrá ver tu Athlete Card ni enviarte mensajes.";
    }

    // Un endpoint simple para que pruebes que el servicio está vivo desde el navegador
    @GetMapping("/estado")
    public String estadoServicio() {
        return "Servicio de Safety activo: Gestionando modo seguro, reportes y bloqueos.";
    }
}
