package com.focus.auth;

import com.focus.common.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(
        @Value("${focus.jwt.secret}") String secret,
        @Value("${focus.jwt.expiration-minutes:480}") long expirationMinutes) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret debe tener al menos 32 caracteres");
        }
        // Fail-fast en perfil prod: rechazar el placeholder por defecto.
        // Checklist seguridad #1: sin secretos por defecto en codigo.
        if (secret.startsWith("cambia-esto")) {
            String activeProfiles = System.getProperty("spring.profiles.active", "");
            if (activeProfiles.contains("prod") || activeProfiles.contains("staging")) {
                throw new IllegalStateException(
                    "JWT_SECRET placeholder detectado en perfil prod/staging. "
                    + "Configurar variable de entorno JWT_SECRET con un valor real.");
            }
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generar(String email, Rol rol) {
        Instant ahora = Instant.now();
        Instant exp = ahora.plus(Duration.ofMinutes(expirationMinutes));
        return Jwts.builder()
            .subject(email)
            .claim("rol", rol.name())
            .issuedAt(Date.from(ahora))
            .expiration(Date.from(exp))
            .signWith(key)
            .compact();
    }

    public Claims validar(String token) {
        Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        return jws.getPayload();
    }
}
