package com.example.medical_be.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.medical_be.properties.AppProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

   
    private static final String RAW_KEY = "01234567890123456789012345678912";

    private JwtService jwtService;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.setKey(Base64.getEncoder().encodeToString(RAW_KEY.getBytes()));

        jwtService = new JwtService(appProperties);
        secretKey = Keys.hmacShaKeyFor(appProperties.getSignKey());
    }

  

    @Test
    void generationToken_shouldContainUsernameAndUserIdClaims() {
        String token = jwtService.generationToken("hoa@example.com", 42L);

        Jws<Claims> parsed = jwtService.parseToken(token);
        Claims claims = parsed.getPayload();

        assertThat(claims.getSubject()).isEqualTo("hoa@example.com");
        assertThat(claims.get("uid", Long.class)).isEqualTo(42L);
    }

    @Test
    void generationRefreshToken_shouldExpireLaterThanAccessToken() {
        String accessToken = jwtService.generationToken("hoa@example.com", 42L);
        String refreshToken = jwtService.generationRefreshToken("hoa@example.com", 42L);

        Date accessExp = jwtService.parseToken(accessToken).getPayload().getExpiration();
        Date refreshExp = jwtService.parseRefreshToken(refreshToken).getPayload().getExpiration();

        assertThat(refreshExp).isAfter(accessExp);
    }

    @Test
    void getExpirationToken_shouldBeAboutOneHourFromNow() {
        LocalDateTime expiration = jwtService.getExpirationToken();

        LocalDateTime now = LocalDateTime.now();
        assertThat(expiration).isAfter(now.plusMinutes(59)).isBefore(now.plusMinutes(61));
    }


    @Test
    void parseToken_shouldThrow_whenTokenIsTamperedWith() {
        String token = jwtService.generationToken("vinh@example.com", 42L);
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertThrows(JwtException.class, () -> jwtService.parseToken(tampered));
    }

    @Test
    void parseToken_shouldThrow_whenTokenIsGarbage() {
        assertThrows(JwtException.class, () -> jwtService.parseToken("khong-phai-la-jwt"));
    }


    @Test
    void parseToken_shouldThrow_whenTokenIsAlreadyExpired() {
        Instant past = Instant.now().minusSeconds(3600);
        String expiredToken = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject("vinh@example.com")
                .claim("uid", 42L)
                .issuedAt(Date.from(past.minusSeconds(10)))
                .expiration(Date.from(past))
                .signWith(secretKey)
                .compact();

        assertThrows(JwtException.class, () -> jwtService.parseToken(expiredToken));
    }

    @Test
    void generationToken_shouldStillWork_whenUserIdIsNull() {
        String token = jwtService.generationToken("vinh@example.com", null);

        Claims claims = jwtService.parseToken(token).getPayload();
        assertThat(claims.get("uid")).isNull();
    }
}
