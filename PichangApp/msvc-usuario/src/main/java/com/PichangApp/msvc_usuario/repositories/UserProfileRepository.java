package com.PichangApp.msvc_usuario.repositories;

import com.PichangApp.msvc_usuario.models.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
}