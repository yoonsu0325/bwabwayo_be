package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.EvaluationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationItemRepository extends JpaRepository<EvaluationItem, Long> {
}
