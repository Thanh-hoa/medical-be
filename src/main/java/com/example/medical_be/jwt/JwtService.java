package com.example.medical_be.jwt;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.example.medical_be.properties.AppProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class JwtService {
    final SecretKey secretKey;
    final long expirationTimeToken = 3600000; // 1 hour
    final long expirationTimeRefreshToken = 604800000; // 7 days
    
    public JwtService(AppProperties appProperties) {
       this.secretKey = Keys.hmacShaKeyFor(appProperties.getSignKey());
    }

    public String generationToken(String username, Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("uid", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationTimeToken / 1000)))
                .signWith(secretKey)
                .compact();
    }

    public String generationRefreshToken(String username , Long userId){
        Instant now  = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("uid", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationTimeRefreshToken / 1000)))
                .signWith(secretKey)
                .compact();

    }
     //jws : JSON Web Signature
    public Jws<Claims> parseToken(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
    }

    public Jws<Claims> parseRefreshToken(String tokenRefresh){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(tokenRefresh);
    }

    public LocalDateTime getExpirationToken(){
        Instant now = Instant.now();
        return LocalDateTime.ofInstant(now.plusMillis(expirationTimeToken) , ZoneId.systemDefault());
    }

    public LocalDateTime getExpirationTokenRefresh(){
        Instant now = Instant.now();
        return LocalDateTime.ofInstant(now.plusMillis(expirationTimeRefreshToken), ZoneId.systemDefault());
    }
}
