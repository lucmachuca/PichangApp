package com.PichangApp.msvc_usuario.services;

import com.PichangApp.msvc_usuario.models.entities.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user);
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    void deleteById(Long id);
}