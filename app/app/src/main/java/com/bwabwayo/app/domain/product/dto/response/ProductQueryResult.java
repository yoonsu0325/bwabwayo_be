package com.bwabwayo.app.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductQueryResult {
    private ProductSimpleDTO product; // 판매 상품
    private UserSimpleDTO seller; // 판매자
}
