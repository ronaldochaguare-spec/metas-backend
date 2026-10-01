package com.metas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final Key key;
    private final long diasValidez;

    public JwtService(@Value("${app.jwt-secret}") String secreto, @Value("${app.jwt-dias}") long diasValidez) {
        if (secreto == null || secreto.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 caracteres. Genera uno con una cadena aleatoria larga.");
        }
        this.key = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.diasValidez = diasValidez;
    }

    public String generar() {
        Date ahora = new Date();
        Date vence = new Date(ahora.getTime() + Duration.ofDays(diasValidez).toMillis());
        return Jwts.builder().subject("ronaldo").issuedAt(ahora).expiration(vence).signWith(key).compact();
    }

    public boolean esValido(String token) {
        try {
            Claims c = Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build()
                    .parseSignedClaims(token).getPayload();
            return c.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
