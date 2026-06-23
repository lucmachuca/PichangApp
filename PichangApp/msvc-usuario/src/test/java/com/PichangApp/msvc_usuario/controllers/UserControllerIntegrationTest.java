package com.PichangApp.msvc_usuario.controllers;

import com.PichangApp.msvc_usuario.models.entities.User;
import com.PichangApp.msvc_usuario.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private com.PichangApp.msvc_usuario.assemblers.UserModelAssembler userModelAssembler;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void register_ValidUser_ReturnsCreated() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("nuevo_usuario");
        user.setEmail("nuevo@correo.com");
        user.setNombre("Juan");
        user.setApellido("Perez");
        user.setPassword("password123");

        when(userService.save(any(User.class))).thenReturn(user);

        com.PichangApp.msvc_usuario.models.dtos.UserResponseDTO dto = new com.PichangApp.msvc_usuario.models.dtos.UserResponseDTO();
        dto.setId(1L);
        dto.setUsername("nuevo_usuario");
        dto.setEmail("nuevo@correo.com");
        when(userModelAssembler.toModel(any(User.class))).thenReturn(org.springframework.hateoas.EntityModel.of(dto));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("nuevo_usuario"))
                .andExpect(jsonPath("$.email").value("nuevo@correo.com"));
    }

    @Test
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        User user = new User();
        user.setUsername("nuevo_usuario");
        user.setEmail("correo-invalido"); // Correo inválido
        user.setNombre("Juan");
        user.setApellido("Perez");
        user.setPassword("password123");

        // Assuming controller validation works
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_ExistingId_ReturnsUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@correo.com");

        when(userService.findById(1L)).thenReturn(Optional.of(user));
        
        com.PichangApp.msvc_usuario.models.dtos.UserResponseDTO dto = new com.PichangApp.msvc_usuario.models.dtos.UserResponseDTO();
        dto.setId(1L);
        dto.setUsername("testuser");
        dto.setEmail("test@correo.com");
        when(userModelAssembler.toModel(any(User.class))).thenReturn(org.springframework.hateoas.EntityModel.of(dto));

        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}
