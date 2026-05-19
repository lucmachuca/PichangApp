package com.PichangApp.msvc_seguridad.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data // Genera getters y setters ocultos
@Entity // Convierte esta clase en la tabla "reportes"
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID autoincrementable (1, 2, 3...)
    private Long idReporte;

    @Column(nullable = false)
    private UUID idUsuarioDenunciante; // El UUID de quien hace la denuncia

    @Column(nullable = false)
    private UUID idUsuarioDenunciado; // El UUID del tramposo o acosador

    @Column(nullable = false)
    private String motivo; // Ej: "Acoso romántico", "Ausencia al partido"

    @Column(nullable = false)
    private String estado = "PENDIENTE"; // Puede ser PENDIENTE, REVISADO o SANCIONADO

    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte = LocalDateTime.now(); // Guarda la fecha y hora exacta
}