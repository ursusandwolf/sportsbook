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

    @Value("${app.jwt.expiration-ms:3600000}") // 1 час
    private long jwtExpirationMs;

    public String generateToken(SecurityUser userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parse(token);
            return true;
        } catch (Exception e) {
            // В реальном проекте логируйте ошибку (expired, signature invalid и т.д.)
            return false;
        }
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
