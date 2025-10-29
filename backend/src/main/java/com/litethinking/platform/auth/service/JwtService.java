package com.litethinking.platform.auth.service;

import com.litethinking.platform.auth.domain.UserAccount;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final JwtParser jwtParser;
    private final long expirationMinutes;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.secretKey = Keys.hmacShaKeyFor(resolveSecret(secret));
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(this.secretKey)
                .build();
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(UserAccount user) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .addClaims(Map.of("role", user.getRole().name()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return jwtParser.parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isValid(String token) {
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }
    private byte[] resolveSecret(String secret) {
        if (secret != null && secret.length() >= 32 && !secret.contains(" ")) {
            return secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        try {
            return Decoders.BASE64.decode(secret);
        } catch (io.jsonwebtoken.io.DecodingException | IllegalArgumentException ex) {
            return secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
