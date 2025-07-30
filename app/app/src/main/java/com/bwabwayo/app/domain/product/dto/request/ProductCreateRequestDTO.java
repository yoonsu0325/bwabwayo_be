package com.bwabwayo.app.domain.product.dto.request;

import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@ParameterObject
public class ProductCreateRequestDTO {
    private Long categoryId; // 카테고리 ID
    private String title; // 상품 제목
    private String description; // 상품 설명
    private Integer price; // 판매가
    private Integer shippingFee; // 배송비
    private Boolean canNegotiate; // 가격 협상 여부
    private Boolean canDirect; // 직거래 여부
    private Boolean canDelivery; // 택배거래 여부
    private Boolean canVideoCall; // 화상거래 여부
    private List<String> images; // 이미지 URL
}
