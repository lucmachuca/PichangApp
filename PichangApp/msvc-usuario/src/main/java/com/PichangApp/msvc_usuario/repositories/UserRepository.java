package com.PichangApp.msvc_usuario.repositories;

import com.PichangApp.msvc_usuario.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u FROM User u
            JOIN FETCH u.profile p
            WHERE u.enabled = true
              AND u.id <> :excludeId
              AND UPPER(p.deportePrincipal) = :deporte
              AND p.latitud IS NOT NULL
              AND p.longitud IS NOT NULL
              AND p.latitud BETWEEN -90 AND 90
              AND p.longitud BETWEEN -180 AND 180
              AND p.ultimaUbicacionAt IS NOT NULL
              AND p.ultimaUbicacionAt >= :fechaMinimaUbicacion
              AND p.edad IS NOT NULL
              AND p.edad BETWEEN :edadMin AND :edadMax
              AND (:sexoFiltro IS NULL OR UPPER(p.sexo) = :sexoFiltro)
            """)
    List<User> findDiscoverCandidates(
            @Param("excludeId") Long excludeId,
            @Param("deporte") String deporte,
            @Param("edadMin") Integer edadMin,
            @Param("edadMax") Integer edadMax,
            @Param("sexoFiltro") String sexoFiltro,
            @Param("fechaMinimaUbicacion") LocalDateTime fechaMinimaUbicacion
    );
}
