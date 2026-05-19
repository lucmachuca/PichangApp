package com.pichangapp.comunicacion.model;

import com.pichangapp.comunicacion.model.enums.EstadoSala;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A chat sala opened as a direct consequence of a SocialMatch.
 * In a future phase, an Event (the actual pichanga/game) can be linked to this sala.
 */
@Entity
@Table(name = "salas_chat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_social_id", nullable = false, unique = true)
    private Long matchSocialId;

    @Column(name = "usuario_a_id", nullable = false)
    private Long usuarioAId;

    @Column(name = "usuario_b_id", nullable = false)
    private Long usuarioBId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSala estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "salaChat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MensajeChat> mensajes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoSala.ACTIVA;
        }
    }
}
