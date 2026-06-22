package com.PichangApp.msvc_usuario.models.entities;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileUnitTest {

    @Test
    void validarPerfilBasketConAlturaYPosicionNoLanzaExcepcion() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal("BASKET");
        profile.setAtributosDeportivos(Map.of(
                "altura", 180,
                "posicion", "Base"
        ));

        assertDoesNotThrow(profile::validarPerfil);
    }

    @Test
    void validarPerfilBasketSinAlturaLanzaExcepcion() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal("BASKET");
        profile.setAtributosDeportivos(Map.of(
                "posicion", "Base"
        ));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                profile::validarPerfil
        );

        assertTrue(exception.getMessage().contains("altura"));
    }

    @Test
    void validarPerfilBoxeoConPesoYGuardiaNoLanzaExcepcion() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal("BOXEO");
        profile.setAtributosDeportivos(Map.of(
                "peso", 75,
                "guardia", "Ortodoxa"
        ));

        assertDoesNotThrow(profile::validarPerfil);
    }

    @Test
    void validarPerfilBoxeoSinGuardiaLanzaExcepcion() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal("BOXEO");
        profile.setAtributosDeportivos(Map.of(
                "peso", 75
        ));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                profile::validarPerfil
        );

        assertTrue(exception.getMessage().contains("guardia"));
    }

    @Test
    void validarPerfilSinDeporteInicializaAtributosSiSonNulos() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal(null);
        profile.setAtributosDeportivos(null);

        assertDoesNotThrow(profile::validarPerfil);
        assertNotNull(profile.getAtributosDeportivos());
        assertTrue(profile.getAtributosDeportivos().isEmpty());
    }
}