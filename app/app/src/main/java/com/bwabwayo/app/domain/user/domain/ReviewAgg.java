package com.bwabwayo.app.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_agg")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewAgg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "avg_rating", nullable = false)
    private float avgRating;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    public void addReview(float newRating) {
        float total = this.avgRating * this.reviewCount;
        this.reviewCount += 1;
        this.avgRating = (total + newRating) / this.reviewCount;
    }
}


