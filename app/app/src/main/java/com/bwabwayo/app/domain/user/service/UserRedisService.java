package com.bwabwayo.app.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserRedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final long REFRESH_TOKEN_TTL = 7 * 24 * 60 * 60; // 7일
    public void saveRefreshToken(String userId, String refreshToken){
        String key = "RefreshToken:" + userId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(REFRESH_TOKEN_TTL));
    }

    // ✅ 조회
    public String getRefreshToken(String userId) {
        String key = "RefreshToken:" + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    // ✅ 삭제
    public void deleteRefreshToken(String userId) {
        String key = "RefreshToken:" + userId;
        redisTemplate.delete(key);
    }
}
