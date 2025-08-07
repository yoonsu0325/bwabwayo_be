package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.ReviewEvaluationCount;
import com.bwabwayo.app.domain.user.dto.response.UserEvaluationStat;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewEvaluationCountRepository extends JpaRepository<ReviewEvaluationCount, Long> {
    @Query("""
    SELECT new com.bwabwayo.app.domain.user.dto.response.UserEvaluationStat(
        r.item.id,
        i.description,
        r.count
    )
    FROM ReviewEvaluationCount r
    JOIN EvaluationItem i ON r.item.id = i.id
    WHERE r.userId = :userId
    """)
    List<UserEvaluationStat> findEvaluationStatsByUserId(@Param("userId") String userId);

    Optional<ReviewEvaluationCount> findByUserIdAndItem_Id(String userId, Long itemId);

    List<ReviewEvaluationCount> findAllByUserId(String userId);
}
