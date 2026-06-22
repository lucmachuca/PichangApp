package com.PichangApp.msvc_usuario.models.entities;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileUnitTest {

    @Test
    void debeAceptarPerfilBasketConAlturaYPosicion() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal("BASKET");
        profile.setAtributosDeportivos(Map.of(
                "altura", 180,
                "posicion", "Base"
        ));

        assertDoesNotThrow(profile::validarPerfil);
    }

    @Test
    void noDebeAceptarPerfilBasketSinAltura() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal("BASKET");
        profile.setAtributosDeportivos(Map.of(
                "posicion", "Base"
        ));

        assertThrows(
                IllegalArgumentException.class,
                profile::validarPerfil
        );
    }

    @Test
    void noDebeAceptarPerfilBasketSinPosicion() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal("BASKET");
        profile.setAtributosDeportivos(Map.of(
                "altura", 180
        ));

        assertThrows(
                IllegalArgumentException.class,
                profile::validarPerfil
        );
    }

    @Test
    void debeAceptarPerfilBoxeoConPesoYGuardia() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal("BOXEO");
        profile.setAtributosDeportivos(Map.of(
                "peso", 75,
                "guardia", "Ortodoxa"
        ));

        assertDoesNotThrow(profile::validarPerfil);
    }

    @Test
    void noDebeAceptarPerfilBoxeoSinGuardia() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal("BOXEO");
        profile.setAtributosDeportivos(Map.of(
                "peso", 75
        ));

        assertThrows(
                IllegalArgumentException.class,
                profile::validarPerfil
        );
    }

    @Test
    void noDebeAceptarPerfilBoxeoSinPeso() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal("BOXEO");
        profile.setAtributosDeportivos(Map.of(
                "guardia", "Zurda"
        ));

        assertThrows(
                IllegalArgumentException.class,
                profile::validarPerfil
        );
    }

    @Test
    void debeInicializarAtributosSiVienenNulosYNoHayDeporte() {
        UserProfile profile = new UserProfile();
        profile.setDeportePrincipal(null);
        profile.setAtributosDeportivos(null);

        assertDoesNotThrow(profile::validarPerfil);
        assertNotNull(profile.getAtributosDeportivos());
        assertTrue(profile.getAtributosDeportivos().isEmpty());
    }
}