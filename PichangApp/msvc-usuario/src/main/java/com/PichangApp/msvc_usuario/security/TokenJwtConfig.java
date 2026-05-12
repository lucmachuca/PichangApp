package com.PichangApp.msvc.usuario.security;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class TokenJwtConfig {

    // Usamos la misma llave que definiste en tu application.properties
    // Tiene que ser de al menos 32 caracteres para el algoritmo HS256
    public static final String SECRET_KEY_STRING = "clave_secreta_super_segura_para_pichangapp_jwt_2026";

    // JJWT 0.12x requiere construir la llave secreta de esta manera:
    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    public static final String PREFIX_TOKEN = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "application/json";
}