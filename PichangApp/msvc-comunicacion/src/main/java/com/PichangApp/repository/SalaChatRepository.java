package com.PichangApp.repository;

import com.PichangApp.model.SalaChat;
import com.PichangApp.model.enums.EstadoSala;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SalaChatRepository extends JpaRepository<SalaChat, Long> {

    Optional<SalaChat> findBySocialMatchId(Long matchSocialId);

    @Query("SELECT r FROM SalaChat r WHERE (r.usuarioAId = :userId OR r.usuarioBId = :userId) AND r.estado = :estado")
    List<SalaChat> findByUserIdAndStatus(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("estado") EstadoSala estado);
}
