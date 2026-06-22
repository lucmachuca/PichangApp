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
    void updateUserProfileActualizaPerfilBasketExistente() {
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

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
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
}