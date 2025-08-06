package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.EvaluationItem;
import com.bwabwayo.app.domain.user.repository.EvaluationItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationItemService {
    private final EvaluationItemRepository evaluationItemRepository;

    public List<EvaluationItem> getEvaluationItems() {
        return evaluationItemRepository.findAll();
    }
}
