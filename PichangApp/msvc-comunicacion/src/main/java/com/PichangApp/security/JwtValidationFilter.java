package com.PichangApp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtro que valida el token JWT presente en la cabecera Authorization de
 * cada solicitud.  Si el token es válido, se establece la autenticación
 * correspondiente en el contexto de seguridad.  En caso contrario, se
 * responde con un error 401 y un mensaje JSON consistente.  Esta clase
 * copia la implementación del microservicio de usuario para mantener
 * consistencia entre servicios.
 */
public class JwtValidationFilter extends BasicAuthenticationFilter {

    public JwtValidationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String header = request.getHeader(TokenJwtConfig.HEADER_AUTHORIZATION);
        if (header == null || !header.startsWith(TokenJwtConfig.PREFIX_TOKEN)) {
            chain.doFilter(request, response);
            return;
        }
        String token = header.replace(TokenJwtConfig.PREFIX_TOKEN, "");
        try {
            Claims claims = Jwts.parser().verifyWith(TokenJwtConfig.SECRET_KEY).build()
                    .parseSignedClaims(token).getPayload();
            String username = claims.getSubject();
            Object authoritiesClaims = claims.get("authorities");
            Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
            if (authoritiesClaims instanceof Collection<?> values) {
                authorities = values.stream()
                        .map(Object::toString)
                        .map(SimpleGrantedAuthority::new)
                        .toList();
            } else if (authoritiesClaims instanceof String value && !value.isBlank()) {
                String[] values = new ObjectMapper().readValue(value, String[].class);
                authorities = java.util.Arrays.stream(values)
                        .map(SimpleGrantedAuthority::new)
                        .toList();
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            chain.doFilter(request, response);
        } catch (JwtException e) {
            Map<String, String> body = new HashMap<>();
            body.put("error", e.getMessage());
            body.put("message", "¡El token JWT es inválido o ha expirado!");
            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        }
    }
}
