package com.bwabwayo.app.domain.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ParameterObject
public class ProductCreateRequestDTO {
    @NotBlank
    private String title; // 상품 제목
    @NotBlank
    private String description; // 상품 설명

    @NotNull
    @Min(0)
    private Integer price; // 판매가
    @Min(0)
    private Integer shippingFee; // 배송비

    @NotNull
    private Boolean canNegotiate; // 가격 협상 여부
    @NotNull
    private Boolean canDirect; // 직거래 여부
    @NotNull
    private Boolean canDelivery; // 택배거래 여부
    @NotNull
    private Boolean canVideoCall; // 화상거래 여부

    @NotNull
    private Long categoryId; // 카테고리 ID

    @Size(min = 1, max = 10)
    private List<String> images; // 이미지 URL
}
