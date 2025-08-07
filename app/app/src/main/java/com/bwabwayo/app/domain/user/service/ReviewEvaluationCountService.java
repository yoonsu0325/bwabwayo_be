package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.ReviewEvaluationCount;
import com.bwabwayo.app.domain.user.dto.response.UserEvaluationStat;
import com.bwabwayo.app.domain.user.repository.ReviewEvaluationCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewEvaluationCountService {
    private final ReviewEvaluationCountRepository reviewEvaluationCountRepository;

    public List<UserEvaluationStat> getEvaluationStats(String userId) {
        return reviewEvaluationCountRepository.findEvaluationStatsByUserId(userId);
    }

    public Optional<ReviewEvaluationCount> getReviewEvaluationCount(String userId, Long itemId) {
        return reviewEvaluationCountRepository.findByUserIdAndItem_Id(userId, itemId);
    }

    public void saveReviewEvaluationCount(ReviewEvaluationCount reviewEvaluationCount) {
        reviewEvaluationCountRepository.save(reviewEvaluationCount);
    }

    public List<ReviewEvaluationCount> getAllByUserId(String userId) {
        return reviewEvaluationCountRepository.findAllByUserId(userId);
    }

}
