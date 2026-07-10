package com.PichangApp.repository;

import com.PichangApp.model.SquadMiembro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SquadMiembroRepository extends JpaRepository<SquadMiembro, Long> {

    boolean existsBySquadIdAndUsuarioId(Long squadId, Long usuarioId);

    long countBySquadId(Long squadId);

    List<SquadMiembro> findByUsuarioIdOrderByFechaUnionDesc(Long usuarioId);

    List<SquadMiembro> findBySquadIdOrderByFechaUnionAsc(Long squadId);

    Optional<SquadMiembro> findBySquadIdAndUsuarioId(Long squadId, Long usuarioId);
}
