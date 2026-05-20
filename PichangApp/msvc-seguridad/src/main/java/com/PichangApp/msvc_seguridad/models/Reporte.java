package com.PichangApp.msvc_seguridad.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReporte;

    @Column(nullable = false)
    private Long idUsuarioDenunciante;

    @Column(nullable = false)
    private Long idUsuarioDenunciado;

    @Column(nullable = false, length = 100)
    private String motivo;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaReporte;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = "PENDIENTE";
        }
        if (this.fechaReporte == null) {
            this.fechaReporte = LocalDateTime.now();
        }
    }
}