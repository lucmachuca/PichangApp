package com.PichangApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "mensajes_squad",
        indexes = {
                @Index(name = "idx_mensajes_squad_squad_id", columnList = "squad_id"),
                @Index(name = "idx_mensajes_squad_fecha_envio", columnList = "fecha_envio")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeSquad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id", nullable = false)
    private Squad squad;

    @Column(name = "remitente_id", nullable = false)
    private Long remitenteId;

    @Column(nullable = false, length = 2000)
    private String contenido;

    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    @PrePersist
    protected void onCreate() {
        fechaEnvio = LocalDateTime.now();
    }
}