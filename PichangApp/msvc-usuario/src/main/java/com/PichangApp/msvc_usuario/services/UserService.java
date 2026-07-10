package com.PichangApp.msvc_usuario.services;

import com.PichangApp.msvc_usuario.models.dtos.UpdateUserProfileDTO;
import com.PichangApp.msvc_usuario.models.entities.User;
import com.PichangApp.msvc_usuario.models.entities.UserProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user);
    List<User> findAll();
    List<User> findDiscoverCandidates(
            Long excludeId,
            String deporte,
            Integer edadMin,
            Integer edadMax,
            String sexoFiltro,
            LocalDateTime fechaMinimaUbicacion
    );
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    void deleteById(Long id);
    UserProfile updateUserProfile(Long userId, UpdateUserProfileDTO dto);
}
