package com.PichangApp.dto;

import com.PichangApp.model.enums.EstadoSala;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

public record SalaChatResponse(
        Long id,
        Long matchSocialId,
        Long usuarioAId,
        Long usuarioBId,
        EstadoSala estado,
        LocalDateTime fechaCreacion
) {}
