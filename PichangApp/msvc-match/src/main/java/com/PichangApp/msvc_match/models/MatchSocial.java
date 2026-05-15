package com.PichangApp.msvc_match.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa una conexiÃ³n social mutua entre dos usuarios (ambos interactuaron
 * con ME_GUSTA).
 * Esto NO es un partido de fÃºtbol, es un enlace de afinidad social que puede
 * desencadenar
 * una sala de chat (ChatRoom) en el servicio de comunicaciÃ³n.
 * Los IDs se normalizan: usuarioAId < usuarioBId para prevenir registros
 * duplicados invertidos.
 */
@Entity
@Table(name = "matches_sociales", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "usuario_a_id", "usuario_b_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_a_id", nullable = false)
    private Long usuarioAId;

    @Column(name = "usuario_b_id", nullable = false)
    private Long usuarioBId;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
    }
}
