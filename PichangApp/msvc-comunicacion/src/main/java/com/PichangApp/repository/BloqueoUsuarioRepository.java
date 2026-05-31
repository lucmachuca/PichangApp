package com.PichangApp.repository;

import com.PichangApp.model.BloqueoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloqueoUsuarioRepository extends JpaRepository<BloqueoUsuario, Long> {

    boolean existsByIdUsuarioOrigenAndIdUsuarioBloqueado(
            Long idUsuarioOrigen,
            Long idUsuarioBloqueado
    );
}