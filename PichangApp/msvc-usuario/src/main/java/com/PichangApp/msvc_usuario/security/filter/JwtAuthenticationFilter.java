package com.PichangApp.msvc_usuario.security.filter;

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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // Parse the incoming JSON into a simple AuthRequest rather than the full
        // User entity.  This avoids unexpected validation failures when only
        // username and password are provided in the login request and prevents
        // serialization issues if the User structure changes.
        String username = null;
        String password = null;
        try {
            com.PichangApp.msvc_usuario.models.dtos.AuthRequest authRequest = new ObjectMapper().readValue(request.getInputStream(), com.PichangApp.msvc_usuario.models.dtos.AuthRequest.class);
            username = authRequest.getUsername();
            password = authRequest.getPassword();
        } catch (IOException e) {
            // If the body cannot be parsed, credentials will remain null and the
            // authentication manager will throw a BadCredentialsException.
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authResult.getPrincipal();
        String username = user.getUsername();

        // Spring Security 7 may include authorities that contain java.time.Instant
        // values (for example FactorGrantedAuthority). Serializing the complete
        // objects caused a 500 error during /login because Jackson needed extra
        // Java Time modules.  For a JWT we only need the authority names, so the
        // token now stores a clean List<String> such as ["ROLE_USER"].
        List<String> roles = authResult.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Claims claims = Jwts.claims()
                .add("authorities", roles)
                .add("username", username)
                .build();

        String token = Jwts.builder()
                .subject(username)
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // Expira en 1 hora
                .issuedAt(new Date())
                .signWith(TokenJwtConfig.SECRET_KEY)
                .compact();

        response.addHeader(TokenJwtConfig.HEADER_AUTHORIZATION, TokenJwtConfig.PREFIX_TOKEN + token);

        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("username", username);
        body.put("message", String.format("Hola %s, ¡has iniciado sesión con éxito!", username));

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        response.setStatus(200);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Error en la autenticación: username o password incorrectos.");
        body.put("error", failed.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        response.setStatus(401);
    }
}