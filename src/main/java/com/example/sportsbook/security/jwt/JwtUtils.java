package com.example.sportsbook.security.jwt;

import com.example.sportsbook.security.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    // В продакшене этот ключ должен быть длиной минимум 256 бит (32 байта)
    @Value("${app.jwt.secret:dGhpcy1pcy1hLXZlcnktc2VjcmV0LWtleS1mb3Itand0LXNwb3J0c2Jvb2stcHJvamVjdC1tdXN0LWJlLWxvbmctZW5vdWdo}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:900000}") // 15 минут
    private long jwtExpirationMs;

    public String generateToken(SecurityUser userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            // В реальном проекте логируйте конкретные исключения: ExpiredJwtException, SignatureException и т.д.
            return null;
        }
    }

    public String getUsernameFromClaims(Claims claims) {
        return claims.getSubject();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
