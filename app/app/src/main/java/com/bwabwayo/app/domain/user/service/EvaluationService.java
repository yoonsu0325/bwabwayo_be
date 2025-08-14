package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.Evaluation;
import com.bwabwayo.app.domain.user.domain.EvaluationItem;
import com.bwabwayo.app.domain.user.repository.EvaluationItemRepository;
import com.bwabwayo.app.domain.user.repository.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final EvaluationRepository evaluationRepository;
    private final EvaluationItemRepository evaluationItemRepository;

    public void saveEvaluation(Evaluation evaluation) {
        evaluationRepository.save(evaluation);
    }

    public List<EvaluationItem> getAll(){
        return evaluationItemRepository.findAll();
    }
}
