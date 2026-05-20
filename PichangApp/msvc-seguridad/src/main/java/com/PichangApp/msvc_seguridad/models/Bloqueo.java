package com.PichangApp.msvc_seguridad.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "bloqueos",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_usuario_origen", "id_usuario_bloqueado"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bloqueo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBloqueo;

    @Column(name = "id_usuario_origen", nullable = false)
    private Long idUsuarioOrigen;

    @Column(name = "id_usuario_bloqueado", nullable = false)
    private Long idUsuarioBloqueado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaBloqueo;

    @PrePersist
    public void prePersist() {
        if (this.fechaBloqueo == null) {
            this.fechaBloqueo = LocalDateTime.now();
        }
    }
}