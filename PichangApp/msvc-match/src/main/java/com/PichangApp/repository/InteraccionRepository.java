package com.PichangApp.repository;

import com.PichangApp.model.Interaccion;
import com.PichangApp.model.enums.TipoInteraccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InteraccionRepository extends JpaRepository<Interaccion, Long> {

    Optional<Interaccion> findByUsuarioOrigenIdAndUsuarioDestinoId(
            Long usuarioOrigenId,
            Long usuarioDestinoId
    );

    boolean existsByUsuarioOrigenIdAndUsuarioDestinoId(
            Long usuarioOrigenId,
            Long usuarioDestinoId
    );

    Optional<Interaccion> findByUsuarioOrigenIdAndUsuarioDestinoIdAndTipo(
            Long usuarioOrigenId,
            Long usuarioDestinoId,
            TipoInteraccion tipo
    );

    List<Interaccion> findByUsuarioOrigenId(Long usuarioOrigenId);
}