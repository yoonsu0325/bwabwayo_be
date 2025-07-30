package com.bwabwayo.app.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequestDTO {
    @NotBlank
    private String title; // 상품 제목

    private String description; // 상품 설명

    @NotNull
    private Integer price; // 판매가
    private Integer shippingFee; // 배송비

    private Boolean canNegotiate; // 가격 협상 여부
    private Boolean canDirect; // 직거래 여부
    private Boolean canDelivery; // 택배거래 여부
    private Boolean canVideoCall; // 화상거래 여부

    private Long categoryId; // 카테고리 ID

    private List<String> images; // 이미지 URL
}
