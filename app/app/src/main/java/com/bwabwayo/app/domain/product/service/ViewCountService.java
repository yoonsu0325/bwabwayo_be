package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
public class ViewCountService {

    private static final Duration VIEW_TTL = Duration.ofHours(1);
    private static final String PREFIX = "viewCount:";

    private final RedisTemplate<String, Long> viewCountRedisTemplate;
    private final RedisTemplate<String, String> viewerRedisTemplate;
    private final ProductRepository productRepository;


    public ViewCountService(
            @Qualifier("redisViewCountTemplate") RedisTemplate<String, Long> viewCountRedisTemplate,
            @Qualifier("redisViewerTemplate") RedisTemplate<String, String> viewerRedisTemplate,
            ProductRepository productRepository
    ) {
        this.viewCountRedisTemplate = viewCountRedisTemplate;
        this.viewerRedisTemplate = viewerRedisTemplate;
        this.productRepository = productRepository;
    }


    /**
     * 현재 조회수를 반환합니다.
     */
    public Long getViewCount(String targetId) {
        String viewCountKey = "viewCount:" + targetId;
        Long count = viewCountRedisTemplate.opsForValue().get(viewCountKey);
        return count != null ? count : 0L;
    }

    /**
     * 조회수를 1 증가시킵니다. 중복 조회자는 제외됩니다.
     */
    public Long increaseViewCount(String targetId, String userIdOrIpAddress) {
        String viewCountKey = "viewCount:" + targetId;
        String viewerSetKey = "viewed:" + targetId;

        ValueOperations<String, Long> valueOps = viewCountRedisTemplate.opsForValue();
        SetOperations<String, String> setOps = viewerRedisTemplate.opsForSet();

        // 중복 조회 체크
        Boolean alreadyViewed = setOps.isMember(viewerSetKey, userIdOrIpAddress);
        if (Boolean.TRUE.equals(alreadyViewed)) {
            return valueOps.get(viewCountKey); // 기존 조회수 반환
        }

        // 중복 아니면 조회수 증가 + 조회자 기록
        Long newCount = valueOps.increment(viewCountKey);
        setOps.add(viewerSetKey, userIdOrIpAddress);
        viewerRedisTemplate.expire(viewerSetKey, VIEW_TTL);

        return newCount;
    }

    public void syncAllToDatabase() {
        Set<String> keys = viewCountRedisTemplate.keys(PREFIX + "*");
        if (keys.isEmpty()) return;

        for (String redisKey : keys) {
            String sTargetId = redisKey.replace(PREFIX, "");
            Long lTargetId = Long.valueOf(sTargetId);

            Long count = viewCountRedisTemplate.opsForValue().get(redisKey);

            if (count == null) continue;

            // RDB에 update
            Product product = productRepository.findById(lTargetId).orElse(null);
            if (product == null) continue;

            product.setViewCount(count.intValue());
        }
    }
}
