package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.ReviewAgg;
import com.bwabwayo.app.domain.user.repository.ReviewAggRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewAggService {
    private final ReviewAggRepository reviewAggRepository;

    public ReviewAgg getOrCreateReviewAgg(String userId) {
        return reviewAggRepository.findByUserId(userId)
                .orElse(ReviewAgg.builder().userId(userId).build());
    }

    public ReviewAgg getReviewAggOrNull(String userId) {
        return reviewAggRepository.findByUserId(userId).orElse(null);
    }

    public Float getAvgRating(String userId){
        return reviewAggRepository
                .findByUserId(userId)
                .map(ReviewAgg::getAvgRating)
                .orElse(0f);
    }

    public int getReviewCount(String userId){
        return reviewAggRepository
                .findByUserId(userId)
                .map(ReviewAgg::getReviewCount)
                .orElse(0);
    }

    public void saveReviewAgg(ReviewAgg reviewAgg){
        reviewAggRepository.save(reviewAgg);
    }
}
