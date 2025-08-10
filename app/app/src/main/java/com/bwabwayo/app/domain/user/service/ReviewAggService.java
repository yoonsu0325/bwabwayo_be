package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.ReviewAgg;
import com.bwabwayo.app.domain.user.repository.ReviewAggRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewAggService {
    private final ReviewAggRepository reviewAggRepository;

    public Optional<ReviewAgg> getReviewAgg(String userId) {
        return reviewAggRepository.findByUserId(userId);
    }
    

    public Float getAvgRating(String userId){
        return reviewAggRepository
                .findByUserId(userId)
                .map(ReviewAgg::getAvgRating)
                .map(avg -> Math.round(avg * 10) / 10f) // 둘째자리에서 반올림 → 첫째자리까지
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

    @Transactional
    public void updateReviewAgg(String sellerId, float rating) {
        ReviewAgg agg = reviewAggRepository.findByUserId(sellerId)
                .orElseGet(() -> ReviewAgg.builder()
                        .userId(sellerId)
                        .reviewCount(0)
                        .avgRating(0f)
                        .build());
        agg.addReview(rating); // 도메인 메서드

        reviewAggRepository.save(agg);
    }
}
