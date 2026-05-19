package com.PichangApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un mensaje enviado dentro de PichangApp.  Un mensaje
 * siempre está asociado a un MatchSocial (idMatch) y guarda el identificador
 * del usuario emisor (idEmisor).  Todos los identificadores utilizan
 * java.util.UUID para cumplir con la regla de oro indicada en la arquitectura.
 */
@Entity
@Table(name = "mensajes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {

    /**
     * Identificador único del mensaje.  Se genera de manera automática
     * utilizando la estrategia GenerationType.UUID para evitar colisiones y
     * dependencia de secuencias en la base de datos.  Esta es la única
     * columna autogenerada de la entidad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_mensaje", updatable = false, nullable = false)
    private UUID idMensaje;

    /**
     * Identificador del MatchSocial al que pertenece el mensaje.  Es un UUID
     * porque los servicios de PichangApp deben manejar identificadores
     * universales.  Esta columna no es una clave foránea real porque la
     * relación existe en el microservicio de match; aquí se guarda para
     * referenciarlo y se valida mediante un cliente Feign.
     */
    @Column(name = "id_match", nullable = false)
    private UUID idMatch;

    /**
     * Identificador del usuario que envió el mensaje.  También se modela
     * como UUID para mantener consistencia con los microservicios que
     * administran usuarios.  Antes de almacenar el mensaje, se validará
     * que este usuario pertenezca al MatchSocial indicado por idMatch.
     */
    @Column(name = "id_emisor", nullable = false)
    private UUID idEmisor;

    /**
     * Contenido textual del mensaje.  Se limita a 2000 caracteres para
     * evitar mensajes excesivamente largos y permitir una validación
     * sencilla a nivel de DTO.
     */
    @Column(nullable = false, length = 2000)
    private String contenido;

    /**
     * Marca la fecha y hora exacta en que el mensaje fue enviado.  Se
     * asigna automáticamente en el evento de persistencia.
     */
    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    /**
     * Indica si el mensaje ha sido leído por el destinatario.  Por
     * defecto, los mensajes se crean como no leídos (false).  Este campo
     * puede utilizarse en futuras ampliaciones para destacar mensajes
     * pendientes.
     */
    @Column(nullable = false)
    private Boolean leido = Boolean.FALSE;

    @PrePersist
    protected void onCreate() {
        this.fechaEnvio = LocalDateTime.now();
        if (this.leido == null) {
            this.leido = Boolean.FALSE;
        }
    }
}