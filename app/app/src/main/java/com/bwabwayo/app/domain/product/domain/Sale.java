package com.bwabwayo.app.domain.product.domain;

import com.bwabwayo.app.domain.product.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1:1 Product
    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 판매자/구매자
    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(name = "buyer_id", nullable = false)
    private String buyerId;

    // 거래 가격
    @Column(name = "sale_price", nullable = false)
    private Integer salePrice;

    // 생성/수정 시각
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 리뷰 여부
    @Column(name = "is_reviewed", nullable = false)
    private boolean isReviewed;

    // 채팅방 id
    @Column(name = "room_id")
    private Long roomId;

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // ==== 결제 단계별 시각 정보 ====
    @Column(name = "payment_requested_at")
    private LocalDateTime paymentRequestedAt;

    @Column(name = "payment_completed_at")
    private LocalDateTime paymentCompletedAt;

    @Column(name = "payment_canceled_at")
    private LocalDateTime paymentCanceledAt;

    @Column(name = "payment_failed_at")
    private LocalDateTime paymentFailedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;


    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        this.createdAt = now;
        this.updatedAt = now;

        // 최초 상태가 PENDING이면 요청 시각도 같이 기록
        if (this.paymentStatus == PaymentStatus.PENDING && this.paymentRequestedAt == null) {
            this.paymentRequestedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public void changePaymentStatus(PaymentStatus newStatus) {
        if (newStatus == null || newStatus == this.paymentStatus) return;

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        switch (newStatus) {
            case PENDING -> {
                if (this.paymentRequestedAt == null) this.paymentRequestedAt = now;
            }
            case COMPLETED -> this.paymentCompletedAt = now;
            case CANCELED  -> this.paymentCanceledAt = now;
            case FAILED    -> this.paymentFailedAt = now;
            case REFUNDED  -> this.refundedAt = now;
        }
        this.paymentStatus = newStatus;
        this.updatedAt = now;
    }
}
