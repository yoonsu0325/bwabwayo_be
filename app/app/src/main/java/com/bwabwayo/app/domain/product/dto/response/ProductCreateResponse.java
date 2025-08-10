package com.bwabwayo.app.domain.product.dto.response;

import com.bwabwayo.app.domain.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateResponse {
    private Long id;

    public static ProductCreateResponse from(Product product){
        return ProductCreateResponse.builder().id(product.getId()).build();
    }
}
