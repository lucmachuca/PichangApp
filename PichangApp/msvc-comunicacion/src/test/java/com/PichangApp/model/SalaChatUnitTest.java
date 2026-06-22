package com.PichangApp.model;

import com.PichangApp.model.enums.EstadoSala;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SalaChatUnitTest {

    @Test
    void onCreateAsignaEstadoActivaYFechaCreacion() {
        SalaChat sala = new SalaChat();

        sala.onCreate();

        assertEquals(EstadoSala.ACTIVA, sala.getEstado());
        assertNotNull(sala.getFechaCreacion());
    }

    @Test
    void onCreateMantieneEstadoExistente() {
        SalaChat sala = new SalaChat();
        sala.setEstado(EstadoSala.ARCHIVADA);

        sala.onCreate();

        assertEquals(EstadoSala.ARCHIVADA, sala.getEstado());
        assertNotNull(sala.getFechaCreacion());
    }
}