package com.PichangApp.msvc_usuario.security.filter;

import com.PichangApp.msvc_usuario.models.entities.User;
import com.PichangApp.msvc_usuario.repositories.UserRepository;
import com.PichangApp.msvc_usuario.security.TokenJwtConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {

        String username = null;
        String password = null;

        try {
            com.PichangApp.msvc_usuario.models.dtos.AuthRequest authRequest =
                    new ObjectMapper().readValue(
                            request.getInputStream(),
                            com.PichangApp.msvc_usuario.models.dtos.AuthRequest.class
                    );

            username = authRequest.getUsername();
            password = authRequest.getPassword();

        } catch (IOException e) {
            // Si el body viene mal, username/password quedan null y Spring Security rechaza el login.
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) throws IOException, ServletException {

        org.springframework.security.core.userdetails.User springUser =
                (org.springframework.security.core.userdetails.User) authResult.getPrincipal();

        String username = springUser.getUsername();

        Optional<User> userOptional = userRepository.findByUsername(username);
        Long userId = userOptional.map(User::getId).orElse(null);

        List<String> roles = authResult.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Claims claims = Jwts.claims()
                .add("authorities", roles)
                .add("username", username)
                .add("userId", userId)
                .build();

        String token = Jwts.builder()
                .subject(username)
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + 7776000000L))
                .issuedAt(new Date())
                .signWith(TokenJwtConfig.SECRET_KEY)
                .compact();

        response.addHeader(
                TokenJwtConfig.HEADER_AUTHORIZATION,
                TokenJwtConfig.PREFIX_TOKEN + token
        );

        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("username", username);
        body.put("userId", userId);
        body.put("message", String.format("Hola %s, ¡has iniciado sesión con éxito!", username));

        response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException, ServletException {

        Map<String, String> body = new HashMap<>();
        body.put("message", "Error en la autenticación: username o password incorrectos.");
        body.put("error", failed.getMessage());

        response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}