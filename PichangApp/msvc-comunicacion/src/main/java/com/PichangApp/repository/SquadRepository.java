package com.PichangApp.repository;

import com.PichangApp.model.Squad;
import com.PichangApp.model.enums.EstadoSquad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SquadRepository extends JpaRepository<Squad, Long> {

    List<Squad> findByEstadoOrderByFechaCreacionDesc(EstadoSquad estado);
}