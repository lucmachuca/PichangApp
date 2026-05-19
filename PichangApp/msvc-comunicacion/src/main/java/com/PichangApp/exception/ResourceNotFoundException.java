package com.PichangApp.exception;

/**
 * Excepción de dominio lanzada cuando una entidad solicitada no se encuentra
 * en la base de datos o no existe en un microservicio externo.  Se utiliza
 * para comunicar un error semántico claro a las capas superiores y a los
 * clientes REST a través del manejador global de excepciones.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}