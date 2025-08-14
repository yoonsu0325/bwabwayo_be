package com.bwabwayo.app.domain.user.domain;

import com.bwabwayo.app.domain.user.dto.request.ReviewRequest;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buyer_id", nullable = false)
    private String buyerId; // 작성자

    @Column(name = "seller_id", nullable = false)
    private String sellerId; // 판매자

    @Column(name = "product_id", nullable = false)
    private Long productId; // 상품

    @Column(nullable = false)
    private float rating; // 평점

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> evaluations = new ArrayList<>();
}
