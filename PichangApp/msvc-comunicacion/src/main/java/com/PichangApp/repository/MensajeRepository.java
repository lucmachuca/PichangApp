package com.PichangApp.repository;

import com.PichangApp.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad {@link Mensaje}.  Extiende de JpaRepository
 * proporcionando operaciones CRUD básicas y expone un método
 * adicional para recuperar todos los mensajes asociados a un match
 * ordenados por fecha de envío de manera ascendente.
 */
public interface MensajeRepository extends JpaRepository<Mensaje, UUID> {

    /**
     * Devuelve la lista de mensajes de un match ordenados por fechaEnvio ascendente.
     *
     * @param idMatch identificador del MatchSocial
     * @return lista de mensajes asociados al match
     */
    List<Mensaje> findByIdMatchOrderByFechaEnvioAsc(UUID idMatch);
}