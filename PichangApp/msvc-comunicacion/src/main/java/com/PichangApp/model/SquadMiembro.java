package com.PichangApp.model;

import com.PichangApp.model.enums.RolSquad;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "squad_miembros",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_squad_miembro", columnNames = {"squad_id", "usuario_id"})
        },
        indexes = {
                @Index(name = "idx_squad_miembros_usuario_id", columnList = "usuario_id"),
                @Index(name = "idx_squad_miembros_squad_id", columnList = "squad_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadMiembro {

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
    private RolSquad rol;

    @Column(name = "fecha_union", nullable = false, updatable = false)
    private LocalDateTime fechaUnion;

    @PrePersist
    protected void onCreate() {
        fechaUnion = LocalDateTime.now();

        if (rol == null) {
            rol = RolSquad.MIEMBRO;
        }
    }
}
