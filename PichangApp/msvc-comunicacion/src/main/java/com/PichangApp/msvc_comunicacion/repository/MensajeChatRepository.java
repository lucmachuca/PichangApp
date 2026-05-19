package com.pichangapp.comunicacion.repository;

import com.pichangapp.comunicacion.model.MensajeChat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {

    Page<MensajeChat> findBySalaChatIdOrderByFechaEnvioDesc(Long salaId, Pageable pageable);
}
