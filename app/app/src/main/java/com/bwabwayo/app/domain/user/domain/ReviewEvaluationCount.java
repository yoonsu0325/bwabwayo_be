package com.bwabwayo.app.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_evaluation_count")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewEvaluationCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 평가 항목 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private EvaluationItem item;

    // 사용자 ID (FK로 안 맺고 문자열로 관리)
    @Column(name = "user_id", nullable = false)
    private String userId;

    // 항목이 평가된 총 개수
    @Column(nullable = false)
    private int count;

    public void increment() {
        count++;
    }
}
