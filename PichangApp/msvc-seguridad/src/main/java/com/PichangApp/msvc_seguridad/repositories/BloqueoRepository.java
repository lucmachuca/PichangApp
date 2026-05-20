package com.PichangApp.msvc_seguridad.repositories;

import com.PichangApp.msvc_seguridad.models.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BloqueoRepository extends JpaRepository<Bloqueo, Long> {

    List<Bloqueo> findByIdUsuarioOrigen(Long idUsuarioOrigen);

    Optional<Bloqueo> findByIdUsuarioOrigenAndIdUsuarioBloqueado(
            Long idUsuarioOrigen,
            Long idUsuarioBloqueado
    );
}