package com.PichangApp.msvc_seguridad.dtos;

import java.time.LocalDateTime;

public record ReporteResponse(
        Long idReporte,
        Long idUsuarioDenunciante,
        Long idUsuarioDenunciado,
        String motivo,
        String descripcion,
        String estado,
        LocalDateTime fechaReporte
) {
}
