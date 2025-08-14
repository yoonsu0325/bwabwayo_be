package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.ReviewAgg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ReviewAggRepository extends JpaRepository<ReviewAgg, Long> {
    Optional<ReviewAgg> findByUserId(String userId);
}
