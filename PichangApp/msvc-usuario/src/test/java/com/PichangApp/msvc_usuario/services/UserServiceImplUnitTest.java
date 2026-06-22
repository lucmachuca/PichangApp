package com.PichangApp.msvc_usuario.services;

import com.PichangApp.msvc_usuario.models.dtos.UpdateUserProfileDTO;
import com.PichangApp.msvc_usuario.models.entities.User;
import com.PichangApp.msvc_usuario.models.entities.UserProfile;
import com.PichangApp.msvc_usuario.repositories.RoleRepository;
import com.PichangApp.msvc_usuario.repositories.UserProfileRepository;
import com.PichangApp.msvc_usuario.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void DebeActualizarPerfilBasketDeUnUsuarioExistente() {
        User user = new User();
        user.setId(1L);
        user.setUsername("ep3_user_a");

        UserProfile profile = new UserProfile();
        profile.setId(10L);
        profile.setUser(user);
        user.setProfile(profile);

        UpdateUserProfileDTO dto = new UpdateUserProfileDTO(
                "Jugador base para prueba EP3",
                22,
                "BASKET",
                Map.of(
                        "altura", 180,
                        "posicion", "Base"
                )
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile resultado = userService.updateUserProfile(1L, dto);

        assertEquals("Jugador base para prueba EP3", resultado.getDescripcion());
        assertEquals(22, resultado.getEdad());
        assertEquals("BASKET", resultado.getDeportePrincipal());
        assertEquals(180, resultado.getAtributosDeportivos().get("altura"));
        assertEquals("Base", resultado.getAtributosDeportivos().get("posicion"));
        assertEquals(user, resultado.getUser());

        verify(userRepository).findById(1L);
        verify(userProfileRepository).save(profile);
    }

    @Test
    void DebeCrearPerfilSiElUsuarioNoTeniaPerfil() {
        User user = new User();
        user.setId(2L);
        user.setUsername("ep3_user_b");
        user.setProfile(null);

        UpdateUserProfileDTO dto = new UpdateUserProfileDTO(
                "Boxeador de prueba",
                24,
                "BOXEO",
                Map.of(
                        "peso", 75,
                        "guardia", "Ortodoxa"
                )
        );

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile resultado = userService.updateUserProfile(2L, dto);

        assertEquals("Boxeador de prueba", resultado.getDescripcion());
        assertEquals(24, resultado.getEdad());
        assertEquals("BOXEO", resultado.getDeportePrincipal());
        assertEquals(75, resultado.getAtributosDeportivos().get("peso"));
        assertEquals("Ortodoxa", resultado.getAtributosDeportivos().get("guardia"));
        assertEquals(user, resultado.getUser());

        verify(userRepository).findById(2L);
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(userRepository).save(user);
    }

    @Test
    void NoDebeActualizarPerfilSiElUsuarioNoExiste() {
        UpdateUserProfileDTO dto = new UpdateUserProfileDTO(
                "Perfil de prueba",
                22,
                "BASKET",
                Map.of(
                        "altura", 180,
                        "posicion", "Base"
                )
        );

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserProfile(999L, dto)
        );

        verify(userRepository).findById(999L);
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void NoDebeAceptarUnDeporteNoSoportado() {
        User user = new User();
        user.setId(1L);
        user.setUsername("ep3_user_a");

        UserProfile profile = new UserProfile();
        profile.setId(10L);
        profile.setUser(user);
        user.setProfile(profile);

        UpdateUserProfileDTO dto = new UpdateUserProfileDTO(
                "Perfil con deporte no soportado",
                22,
                "FUTBOL",
                Map.of(
                        "posicion", "Delantero"
                )
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserProfile(1L, dto)
        );

        verify(userRepository).findById(1L);
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }
}