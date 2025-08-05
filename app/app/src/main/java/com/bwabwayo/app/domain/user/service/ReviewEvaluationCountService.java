package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.ReviewEvaluationCount;
import com.bwabwayo.app.domain.user.dto.response.UserEvaluationStat;
import com.bwabwayo.app.domain.user.repository.ReviewEvaluationCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewEvaluationCountService {
    private final ReviewEvaluationCountRepository reviewEvaluationCountRepository;

    public List<UserEvaluationStat> getEvaluationStats(String userId) {
        return reviewEvaluationCountRepository.findEvaluationStatsByUserId(userId);
    }

}
