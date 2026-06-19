package com.PichangApp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "votos_encuesta", uniqueConstraints = {
    // Un usuario solo puede votar una vez por una opción específica.
    @UniqueConstraint(columnNames = {"opcion_id", "usuario_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VotoEncuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_id", nullable = false)
    private OpcionEncuesta opcion;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_voto", nullable = false, updatable = false)
    private LocalDateTime fechaVoto;

    @PrePersist
    protected void onCreate() {
        this.fechaVoto = LocalDateTime.now();
    }
}
