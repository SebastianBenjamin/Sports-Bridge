package org.hackcelestial.sportsbridge.Api.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlSeconds;

    public JwtService(@Value("${security.jwtSecret:CHANGE_ME_TEST_SECRET_32B}") String secret,
                      @Value("${security.jwtTtlSeconds:604800}") long ttlSeconds) {
        byte[] bytes = secret.getBytes();
        if (bytes.length < 32) {
            byte[] b = new byte[32];
            System.arraycopy(bytes, 0, b, 0, Math.min(bytes.length, 32));
            bytes = b;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.ttlSeconds = ttlSeconds;
    }

    public String issueToken(Long userId, String phone) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of("phone", phone))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}

