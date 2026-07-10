package com.PichangApp.model;

import com.PichangApp.model.enums.EstadoSolicitudSquad;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "squad_solicitudes",
        indexes = {
                @Index(name = "idx_squad_solicitudes_squad_id", columnList = "squad_id"),
                @Index(name = "idx_squad_solicitudes_usuario_id", columnList = "usuario_id"),
                @Index(name = "idx_squad_solicitudes_estado", columnList = "estado")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id", nullable = false)
    private Squad squad;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitudSquad estado;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @PrePersist
    protected void onCreate() {
        fechaSolicitud = LocalDateTime.now();

        if (estado == null) {
            estado = EstadoSolicitudSquad.PENDIENTE;
        }
    }
}
