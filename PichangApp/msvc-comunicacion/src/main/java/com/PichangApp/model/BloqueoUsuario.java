package com.PichangApp.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "bloqueos_usuarios_cache",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_usuario_origen", "id_usuario_bloqueado"})
        }
)
public class BloqueoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idBloqueoOriginal;

    @Column(name = "id_usuario_origen", nullable = false)
    private Long idUsuarioOrigen;

    @Column(name = "id_usuario_bloqueado", nullable = false)
    private Long idUsuarioBloqueado;

    private LocalDateTime fechaBloqueo;

    public Long getId() {
        return id;
    }

    public Long getIdBloqueoOriginal() {
        return idBloqueoOriginal;
    }

    public void setIdBloqueoOriginal(Long idBloqueoOriginal) {
        this.idBloqueoOriginal = idBloqueoOriginal;
    }

    public Long getIdUsuarioOrigen() {
        return idUsuarioOrigen;
    }

    public void setIdUsuarioOrigen(Long idUsuarioOrigen) {
        this.idUsuarioOrigen = idUsuarioOrigen;
    }

    public Long getIdUsuarioBloqueado() {
        return idUsuarioBloqueado;
    }

    public void setIdUsuarioBloqueado(Long idUsuarioBloqueado) {
        this.idUsuarioBloqueado = idUsuarioBloqueado;
    }

    public LocalDateTime getFechaBloqueo() {
        return fechaBloqueo;
    }

    public void setFechaBloqueo(LocalDateTime fechaBloqueo) {
        this.fechaBloqueo = fechaBloqueo;
    }
}