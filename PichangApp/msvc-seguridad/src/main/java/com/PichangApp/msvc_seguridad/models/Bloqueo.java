package com.PichangApp.msvc_seguridad.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "bloqueos")
public class Bloqueo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBloqueo;

    @Column(nullable = false)
    private UUID idUsuarioOrigen; // Quien bloquea

    @Column(nullable = false)
    private UUID idUsuarioBloqueado; // A quien bloquearon

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo = LocalDateTime.now();
}