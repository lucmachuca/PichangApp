package com.PichangApp.model;

import com.PichangApp.model.enums.EstadoSquad;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "squads",
        indexes = {
                @Index(name = "idx_squads_creador_id", columnList = "creador_id"),
                @Index(name = "idx_squads_deporte", columnList = "deporte"),
                @Index(name = "idx_squads_estado", columnList = "estado")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Squad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creador_id", nullable = false)
    private Long creadorId;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 40)
    private String deporte;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "max_integrantes", nullable = false)
    private Integer maxIntegrantes;

    @Column
    private Double latitud;

    @Column
    private Double longitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSquad estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "squad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SquadMiembro> miembros = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();

        if (estado == null) {
            estado = EstadoSquad.ACTIVO;
        }
    }
}