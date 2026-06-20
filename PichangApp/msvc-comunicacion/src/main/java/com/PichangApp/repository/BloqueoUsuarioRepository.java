package com.PichangApp.repository;

import com.PichangApp.model.BloqueoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BloqueoUsuarioRepository extends JpaRepository<BloqueoUsuario, Long> {

    boolean existsByIdUsuarioOrigenAndIdUsuarioBloqueado(
            Long idUsuarioOrigen,
            Long idUsuarioBloqueado
    );

    Optional<BloqueoUsuario> findByIdUsuarioOrigenAndIdUsuarioBloqueado(
            Long idUsuarioOrigen,
            Long idUsuarioBloqueado
    );
}