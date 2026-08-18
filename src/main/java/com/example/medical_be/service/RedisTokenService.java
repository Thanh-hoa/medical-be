package com.example.medical_be.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisTokenService {

    private static final String TOKEN_KEY_PREFIX = "token:";
    private static final String ACTIVE_TOKENS_SET_PREFIX = "user_tokens:";
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:token:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Lưu token vào Redis với TTL
     */
    public void saveToken(Long accountId, String token, LocalDateTime expiredAt) {
        String tokenKey = TOKEN_KEY_PREFIX + token;
        Duration ttl = Duration.between(LocalDateTime.now(), expiredAt);

        if (ttl.isNegative() || ttl.isZero()) {
            log.warn("Token TTL is negative or zero for account {}, skipping", accountId);
            return;
        }

        // Lưu token với TTL tự động
        redisTemplate.opsForValue().set(tokenKey, String.valueOf(accountId), ttl.toSeconds(), TimeUnit.SECONDS);

        // Lưu vào set các token active của user (để cleanup nếu cần)
        String userTokensKey = ACTIVE_TOKENS_SET_PREFIX + accountId;
        redisTemplate.opsForSet().add(userTokensKey, token);
        redisTemplate.expire(userTokensKey, ttl.toSeconds(), TimeUnit.SECONDS);

        log.debug("Token saved to Redis for account {} (TTL: {}s)", accountId, ttl.toSeconds());
    }

    /**
     * Kiểm tra token có hợp lệ trong Redis không
     */
    public boolean isValidToken(String token) {
        String tokenKey = TOKEN_KEY_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(tokenKey);
        if (Boolean.TRUE.equals(exists)) {
            // Kiểm tra blacklist
            String blacklistKey = BLACKLIST_KEY_PREFIX + token;
            Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
            return !Boolean.TRUE.equals(isBlacklisted);
        }
        return false;
    }

    /**
     * Vô hiệu hóa token (xóa khỏi Redis)
     */
    public void deactivateToken(String token) {
        String tokenKey = TOKEN_KEY_PREFIX + token;

        // Lấy accountId trước khi xóa
        Object accountIdObj = redisTemplate.opsForValue().get(tokenKey);
        if (accountIdObj != null) {
            String userTokensKey = ACTIVE_TOKENS_SET_PREFIX + accountIdObj.toString();
            redisTemplate.opsForSet().remove(userTokensKey, token);
        }

        // Xóa token key
        redisTemplate.delete(tokenKey);

        // Thêm vào blacklist với TTL ngắn (để tránh reuse token cũ)
        String blacklistKey = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(blacklistKey, "1", 24, TimeUnit.HOURS);

        log.debug("Token deactivated in Redis");
    }

    /**
     * Vô hiệu hóa tất cả token của một user
     */
    public void deactivateAllUserTokens(Long accountId) {
        String userTokensKey = ACTIVE_TOKENS_SET_PREFIX + accountId;
        Set<Object> tokens = redisTemplate.opsForSet().members(userTokensKey);

        if (tokens != null && !tokens.isEmpty()) {
            for (Object token : tokens) {
                String tokenKey = TOKEN_KEY_PREFIX + token.toString();
                redisTemplate.delete(tokenKey);

                // Thêm vào blacklist với TTL ngắn
                String blacklistKey = BLACKLIST_KEY_PREFIX + token.toString();
                redisTemplate.opsForValue().set(blacklistKey, "1", 24, TimeUnit.HOURS);
            }
            redisTemplate.delete(userTokensKey);
            log.info("All tokens deactivated for account {}", accountId);
        }
    }

    /**
     * Lấy accountId từ token
     */
    public Long getAccountIdFromToken(String token) {
        String tokenKey = TOKEN_KEY_PREFIX + token;
        Object accountIdObj = redisTemplate.opsForValue().get(tokenKey);
        if (accountIdObj != null) {
            return Long.valueOf(accountIdObj.toString());
        }
        return null;
    }

    /**
     * Kiểm tra kết nối Redis
     */
    public boolean isRedisAvailable() {
        try {
            return Boolean.TRUE.equals(redisTemplate.getConnectionFactory().getConnection().ping() != null);
        } catch (Exception e) {
            log.warn("Redis is not available: {}", e.getMessage());
            return false;
        }
    }
}
