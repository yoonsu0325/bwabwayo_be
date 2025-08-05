package com.bwabwayo.app.domain.product.domain;

import com.bwabwayo.app.domain.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 1:1 관계 (하나의 Product는 하나의 Sale만 가짐)
    @OneToOne
    @JoinColumn(name = "product_id", unique = true, nullable = false)
    private Product product;

    // ✅ 판매자/구매자 (소셜 ID 기반 문자열)
    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(name = "buyer_id", nullable = false)
    private String buyerId;

    // ✅ 거래 가격
    @Column(name = "sale_price", nullable = false)
    private Integer salePrice;

    // ✅ 생성 시각 (등록 시 자동 저장)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ✅ 리뷰 완료 여부
    @Column(name = "is_reviewed", nullable = false)
    private boolean isReviewed;

    // ✅ 채팅방 id (예약 거래방 등과 연결)
    @Column(name = "room_id", nullable = true)
    private Long roomId;
}
