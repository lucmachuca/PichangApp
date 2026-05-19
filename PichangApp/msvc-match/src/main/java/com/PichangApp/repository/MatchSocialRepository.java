package com.PichangApp.repository;

import com.PichangApp.model.MatchSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchSocialRepository extends JpaRepository<MatchSocial, Long> {

    @Query("SELECT m FROM MatchSocial m WHERE (m.usuarioAId = :userId OR m.usuarioBId = :userId) AND m.activo = true")
    List<MatchSocial> findActiveMatchesByUserId(Long userId);

    @Query("SELECT m FROM MatchSocial m WHERE " +
            "(m.usuarioAId = :userA AND m.usuarioBId = :userB) OR " +
            "(m.usuarioAId = :userB AND m.usuarioBId = :userA)")
    Optional<MatchSocial> findByUsers(Long userA, Long userB);
}
