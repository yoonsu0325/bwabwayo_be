package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

}
