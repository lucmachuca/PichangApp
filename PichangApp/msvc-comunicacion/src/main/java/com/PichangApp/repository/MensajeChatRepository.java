package com.PichangApp.repository;

import com.PichangApp.model.MensajeChat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {

    Page<MensajeChat> findBySalaChatIdOrderByFechaEnvioDesc(Long salaId, Pageable pageable);

    Optional<MensajeChat> findFirstBySalaChatIdOrderByFechaEnvioDesc(Long salaId);
}
