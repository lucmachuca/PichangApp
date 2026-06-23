package com.PichangApp.msvc_usuario.services;

import com.PichangApp.msvc_usuario.models.dtos.UpdateUserProfileDTO;
import com.PichangApp.msvc_usuario.models.entities.Role;
import com.PichangApp.msvc_usuario.models.entities.User;
import com.PichangApp.msvc_usuario.models.entities.UserProfile;
import com.PichangApp.msvc_usuario.repositories.RoleRepository;
import com.PichangApp.msvc_usuario.repositories.UserProfileRepository;
import com.PichangApp.msvc_usuario.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserProfileRepository userProfileRepository,
                           RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User save(User user) {
        // 0. Validar si el username ya existe (Mejora IL3.2: Completitud y Seguridad)
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("El username ya se encuentra en uso.");
        }

        // 1. Encriptar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2. Asignar Rol por defecto
        Role roleUser = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("ROLE_USER");
            return roleRepository.save(role);
        });

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            List<Role> roles = new ArrayList<>();
            roles.add(roleUser);
            user.setRoles(roles);
        }

        // 3. Validar y asociar el Perfil Dinámico
        if (user.getProfile() != null) {
            validateProfile(user.getProfile());
            user.getProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfile updateUserProfile(Long userId, UpdateUserProfileDTO dto) {
        // 1. Buscar el usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        // 2. Obtener o crear el perfil
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        // 3. Actualizar datos del perfil
        if (dto.getDescripcion() != null) {
            profile.setDescripcion(dto.getDescripcion());
        }
        if (dto.getEdad() != null) {
            profile.setEdad(dto.getEdad());
        }
        if (dto.getDeportePrincipal() != null) {
            profile.setDeportePrincipal(dto.getDeportePrincipal());
        }
        if (dto.getAtributosDeportivos() != null) {
            profile.setAtributosDeportivos(dto.getAtributosDeportivos());
        }

        // 4. Validar el perfil actualizado
        validateProfile(profile);

        // 5. Guardar el perfil
        UserProfile savedProfile = userProfileRepository.save(profile);

        // 6. Actualizar la referencia en el usuario (si es nueva)
        if (user.getProfile() == null) {
            user.setProfile(savedProfile);
            userRepository.save(user);
        }

        return savedProfile;
    }

    private void validateProfile(UserProfile profile) {
        if (profile.getDeportePrincipal() == null) {
            throw new IllegalArgumentException("El deporte principal es obligatorio");
        }

        Map<String, Object> atributos = profile.getAtributosDeportivos();
        if (atributos == null) {
            throw new IllegalArgumentException("Los atributos deportivos no pueden ser nulos");
        }

        switch (profile.getDeportePrincipal().toUpperCase()) {
            case "BASKET" -> {
                if (!atributos.containsKey("altura") || !atributos.containsKey("posicion")) {
                    throw new IllegalArgumentException("Para Basket, la altura y posición son obligatorias.");
                }
            }
            case "BOXEO" -> {
                if (!atributos.containsKey("peso") || !atributos.containsKey("guardia")) {
                    throw new IllegalArgumentException("Para Boxeo, el peso y la guardia son obligatorios.");
                }
            }
            default -> throw new IllegalArgumentException("Deporte no soportado: " + profile.getDeportePrincipal());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}