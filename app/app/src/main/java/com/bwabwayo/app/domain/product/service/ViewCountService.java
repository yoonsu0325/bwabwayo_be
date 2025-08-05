package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Long getViewCount(Long productId) {
        String viewCountKey = "viewCount:" + productId;
        Long count = viewCountRedisTemplate.opsForValue().get(viewCountKey);
        return count != null ? count : 0L;
    }

    /**
     * 조회수를 1 증가시킵니다. 중복 조회자는 제외됩니다.
     */
    public Long increaseViewCount(Long productId, String identifier) {
        String viewCountKey = "viewCount:" + productId;
        String viewerSetKey = "viewed:" + productId;

        ValueOperations<String, Long> valueOps = viewCountRedisTemplate.opsForValue();
        SetOperations<String, String> setOps = viewerRedisTemplate.opsForSet();

        // 중복 조회 체크
        Boolean alreadyViewed = setOps.isMember(viewerSetKey, identifier);
        if (Boolean.TRUE.equals(alreadyViewed)) {
            return valueOps.get(viewCountKey); // 기존 조회수 반환
        }

        // 중복 아니면 조회수 증가 + 조회자 기록
        Long newCount = valueOps.increment(viewCountKey);
        setOps.add(viewerSetKey, identifier);
        viewerRedisTemplate.expire(viewerSetKey, VIEW_TTL);

        return newCount;
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void syncAllToDatabase() {
        // "viewcount:"에 관한 모든 key를 가져옴
        Set<String> keys = viewCountRedisTemplate.keys(PREFIX + "*");
        if (keys.isEmpty()) return;

        for (String redisKey : keys) {
            // productId 추출
            String sTargetId = redisKey.replace(PREFIX, "");
            Long lTargetId = Long.valueOf(sTargetId);

            // Redis에 저장된 상품의 조회수를 가져옴
            Long count = viewCountRedisTemplate.opsForValue().get(redisKey);
            if (count == null) continue;

            // 상품의 조회수를 저장
            Product product = productRepository.findById(lTargetId).orElse(null);
            if (product == null) continue;

            product.setViewCount(count.intValue());
        }
    }
}
