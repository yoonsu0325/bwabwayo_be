package com.bwabwayo.app.domain.product.domain;

import com.bwabwayo.app.domain.product.enums.DeliveryStatus;
import com.bwabwayo.app.domain.product.enums.DeliveryStatusConverter;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.domain.product.enums.SaleStatusConvert;
import com.bwabwayo.app.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 상품 ID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // 상품 카테고리

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false, updatable = false)
    private User seller; // 판매자

    @Column(length = 255, nullable = false)
    private String title; // 판매글 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description; // 판매글 내용

    @Column(nullable = false)
    private int price; // 판매가

    @Column(length = 1024, nullable = false)
    private String thumbnail; // 썸네일 key

    @Setter(AccessLevel.NONE)
    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>(); // 상품 이미지

    @Column(name = "view_count", nullable = false)
    private int viewCount; // 조회 수

    @Column(name = "wish_count", nullable = false)
    private int wishCount; // 관심 수

    @Column(name = "chat_count", nullable = false)
    private int chatCount; // 채팅 수

    @Column(name = "can_negotiate", nullable = false)
    private boolean canNegotiate; // 가격 협상 가능 여부

    @Column(name = "can_direct", nullable = false)
    private boolean canDirect; // 직거래 가능 여부

    @Builder.Default
    @Column(name = "can_delivery", nullable = false)
    private boolean canDelivery = true; // 택배거래 가능 여부

    @Builder.Default
    @Column(name = "can_video_call", nullable = false)
    private boolean canVideoCall = true; // 화상 거래 가능 여부

    @Builder.Default
    @Convert(converter = SaleStatusConvert.class)
    @Column(name = "sale_status", nullable = false)
    private SaleStatus saleStatus = SaleStatus.AVAILABLE; // 판매 상태

    @Column(name = "shipping_fee")
    private Integer shippingFee; // 배송비

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private Courier courier; // 택배사

    @Column(name = "invoice_number", length = 20)
    private String invoiceNumber; // 운송장 번호

    @Convert(converter = DeliveryStatusConverter.class)
    @Column(name = "delivery_status")
    private DeliveryStatus deliveryStatus; // 배송 상태

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 상품 등록 시각

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 상품 최종 수정 시각


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public void addChatCount(){
        this.chatCount += 1;
    }
}
