package com.PichangApp.security;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

/**
 * Configuración estática para el manejo de JSON Web Tokens (JWT).  Se
 * comparte la misma clave secreta que el microservicio de usuario para
 * asegurar que todos los microservicios puedan validar los tokens
 * generados.  En un entorno real, esta clave debería inyectarse a
 * través de configuración externa o variables de entorno.
 */
public class TokenJwtConfig {

    /**
     * Clave secreta empleada para firmar y verificar tokens.  Debe
     * tener al menos 32 caracteres para satisfacer los requisitos del
     * algoritmo HS256.  Se puede externalizar mediante
     * application.properties con la propiedad
     * `config.security.oauth.jwt.key`.
     */
    public static final String SECRET_KEY_STRING = "clave_secreta_super_segura_para_pichangapp_jwt_2026";

    /**
     * Llave secreta derivada de SECRET_KEY_STRING.  Se genera usando
     * la utilidad de JJWT para soportar versiones 0.12.x.
     */
    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    public static final String PREFIX_TOKEN = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "application/json";
}