package com.PichangApp.repository;

import com.PichangApp.model.BloqueoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BloqueoUsuarioRepository extends JpaRepository<BloqueoUsuario, Long> {

    boolean existsByIdUsuarioOrigenAndIdUsuarioBloqueado(
            Long idUsuarioOrigen,
            Long idUsuarioBloqueado
    );

    Optional<BloqueoUsuario> findByIdUsuarioOrigenAndIdUsuarioBloqueado(
            Long idUsuarioOrigen,
            Long idUsuarioBloqueado
    );

    @Query("""
            SELECT b FROM BloqueoUsuario b
            WHERE (b.idUsuarioOrigen = :usuarioId AND b.idUsuarioBloqueado IN :otrosUsuariosIds)
               OR (b.idUsuarioBloqueado = :usuarioId AND b.idUsuarioOrigen IN :otrosUsuariosIds)
            """)
    List<BloqueoUsuario> findBloqueosEntreUsuarioYOtros(
            @Param("usuarioId") Long usuarioId,
            @Param("otrosUsuariosIds") List<Long> otrosUsuariosIds
    );
}
