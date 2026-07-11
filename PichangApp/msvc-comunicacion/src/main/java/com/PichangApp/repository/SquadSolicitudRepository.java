package com.PichangApp.repository;

import com.PichangApp.model.SquadSolicitud;
import com.PichangApp.model.enums.EstadoSolicitudSquad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SquadSolicitudRepository extends JpaRepository<SquadSolicitud, Long> {

    Optional<SquadSolicitud> findBySquadIdAndUsuarioId(Long squadId, Long usuarioId);

    boolean existsBySquadIdAndUsuarioIdAndEstado(
            Long squadId,
            Long usuarioId,
            EstadoSolicitudSquad estado
    );

    List<SquadSolicitud> findBySquadIdAndEstadoOrderByFechaSolicitudDesc(
            Long squadId,
            EstadoSolicitudSquad estado
    );
}
