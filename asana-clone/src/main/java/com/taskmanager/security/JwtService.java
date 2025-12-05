package com.taskmanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtService - Servicio para gestión de tokens JWT
 *
 * CUMPLE REQUERIMIENTO N°1: Autenticación de usuarios
 *
 * JWT (JSON Web Token) se usa para:
 * 1. Autenticación stateless (sin sesiones en servidor)
 * 2. Transmitir información de forma segura
 * 3. Verificar identidad del usuario en cada request
 */
@Service
public class JwtService {

    // Clave secreta para firmar tokens (viene de application.properties)
    @Value("${jwt.secret}")
    private String secretKey;

    // Tiempo de expiración del token (viene de application.properties)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extrae el username (email) del token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT para un usuario
     * N°1: Este método se usa después del login exitoso
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un token con claims adicionales
     *
     * @param extraClaims Claims personalizados (roles, permisos, etc.)
     * @param userDetails Información del usuario
     * @return Token JWT firmado
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims) // Claims personalizados
                .setSubject(userDetails.getUsername()) // Username (email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha emisión
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Expiración
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Firma
                .compact();
    }

    /**
     * Valida si un token es válido para un usuario
     *
     * @param token Token a validar
     * @param userDetails Usuario contra el que validar
     * @return true si el token es válido y no ha expirado
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Verifica si el token ha expirado
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae todos los claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Obtiene la clave de firma a partir del secret
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}