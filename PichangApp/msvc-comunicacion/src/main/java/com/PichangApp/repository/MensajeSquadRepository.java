package com.PichangApp.repository;

import com.PichangApp.model.MensajeSquad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeSquadRepository extends JpaRepository<MensajeSquad, Long> {

    Page<MensajeSquad> findBySquadIdOrderByFechaEnvioDesc(Long squadId, Pageable pageable);
}
