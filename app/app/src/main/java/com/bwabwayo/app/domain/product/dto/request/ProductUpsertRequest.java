package com.bwabwayo.app.domain.product.dto.request;

import com.bwabwayo.app.domain.product.annotation.AtLeastOneTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AtLeastOneTrue(fields = {"canDirect", "canDelivery"}, message = "canDirect 또는 canDelivery 중 하나는 true여야 합니다.")
public class ProductUpsertRequest {
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

    @NotNull
    @Size(min = 1, max = 10)
    private List<String> images; // 이미지 URL
}
