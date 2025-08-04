package com.bwabwayo.app.domain.auth.service;

import com.bwabwayo.app.domain.auth.utils.EncryptUtil;
import com.bwabwayo.app.domain.auth.utils.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;
    private final EncryptUtil encryptUtil;

    // ✅ 저장
    public void saveRefreshToken(String tempId, String userId, String refreshToken) {
        try {
            String key = "token:" + tempId;

            Map<String, String> valueMap = new HashMap<>();
            valueMap.put("userId", encryptUtil.encrypt(userId));
            valueMap.put("refreshToken", encryptUtil.encrypt(refreshToken));

            redisTemplate.opsForHash().putAll(key, valueMap);
            redisTemplate.expire(key, Duration.ofMinutes(jwtProperties.getRefreshExpMinutes()));
        } catch (Exception e) {
            log.error("Redis 저장 중 예외 발생", e);
            throw new IllegalStateException("Redis 저장 실패");
        }
    }

    // ✅ 조회
    public String getDecryptedUserId(String tempId) throws Exception {
        String key = "token:" + tempId;
        Object encrypted = redisTemplate.opsForHash().get(key, "userId");
        return encrypted != null ? encryptUtil.decrypt(encrypted.toString()) : null;
    }

    public String getDecryptedRefreshToken(String tempId) throws Exception {
        String key = "token:" + tempId;
        Object encrypted = redisTemplate.opsForHash().get(key, "refreshToken");
        return encrypted != null ? encryptUtil.decrypt(encrypted.toString()) : null;
    }

    // ✅ 삭제
    public void deleteRefreshToken(String tempId) {
        String key = "token:" + tempId;
        redisTemplate.delete(key);
    }

    // ✅ 존재 여부
    public boolean keyCheck(String key) {
        return redisTemplate.hasKey(key);
    }
}
