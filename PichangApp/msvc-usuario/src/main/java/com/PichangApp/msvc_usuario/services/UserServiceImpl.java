package com.PichangApp.msvc_usuario.services;

import com.PichangApp.msvc_usuario.models.entities.Role;
import com.PichangApp.msvc_usuario.models.entities.User;
import com.PichangApp.msvc_usuario.models.entities.UserProfile;
import com.PichangApp.msvc_usuario.repositories.RoleRepository;
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
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

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

        if (user.getProfile() != null) {
            validateProfile(user.getProfile());
            user.getProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateProfile(Long id, UserProfile profileRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));

        UserProfile profile = user.getProfile();

        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }

        if (profileRequest.getDescripcion() != null) {
            profile.setDescripcion(profileRequest.getDescripcion());
        }

        if (profileRequest.getEdad() != null) {
            profile.setEdad(profileRequest.getEdad());
        }

        if (profileRequest.getFotoUrl() != null) {
            profile.setFotoUrl(profileRequest.getFotoUrl());
        }

        if (profileRequest.getDeportePrincipal() != null) {
            profile.setDeportePrincipal(profileRequest.getDeportePrincipal());
        }

        if (profileRequest.getAtributosDeportivos() != null) {
            profile.setAtributosDeportivos(profileRequest.getAtributosDeportivos());
        }

        profile.setUser(user);
        validateProfile(profile);

        return userRepository.save(user);
    }

    private void validateProfile(UserProfile profile) {
        if (profile.getDeportePrincipal() == null || profile.getDeportePrincipal().isBlank()) {
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
            default -> throw new IllegalArgumentException(
                    "Deporte no soportado: " + profile.getDeportePrincipal()
            );
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