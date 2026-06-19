package com.PichangApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_chat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id", nullable = false)
    private SalaChat salaChat;

    @Column(name = "remitente_id", nullable = false)
    private Long remitenteId;

    @Column(nullable = false, length = 2000)
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_mensaje", nullable = false)
    @Builder.Default
    private com.PichangApp.model.enums.TipoMensaje tipoMensaje = com.PichangApp.model.enums.TipoMensaje.TEXTO;

    @Column(name = "media_url", length = 1000)
    private String mediaUrl;

    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    @PrePersist
    protected void onCreate() {
        this.fechaEnvio = LocalDateTime.now();
    }
}
