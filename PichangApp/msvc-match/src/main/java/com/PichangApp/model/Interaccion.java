package com.PichangApp.model;

import com.PichangApp.model.enums.TipoInteraccion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa una única interacción direccional (ME_GUSTA o DISME_GUSTA) de un
 * usuario hacia otro.
 * Una restricción única (unique constraint) previene interacciones duplicadas
 * hacia el mismo destino.
 */
@Entity
@Table(name = "interacciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "usuario_origen_id", "usuario_destino_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_origen_id", nullable = false)
    private Long usuarioOrigenId;

    @Column(name = "usuario_destino_id", nullable = false)
    private Long usuarioDestinoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInteraccion tipo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
