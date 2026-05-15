package com.PichangApp.msvc_match.repositories;

import com.PichangApp.msvc_match.models.Interaccion;
import com.PichangApp.msvc_match.models.enums.TipoInteraccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InteraccionRepository extends JpaRepository<Interaccion, Long> {

    Optional<Interaccion> findByUsuarioOrigenIdAndUsuarioDestinoIdAndTipo(Long usuarioOrigenId, Long usuarioDestinoId, TipoInteraccion tipo);

    boolean existsByUsuarioOrigenIdAndUsuarioDestinoId(Long usuarioOrigenId, Long usuarioDestinoId);
}
