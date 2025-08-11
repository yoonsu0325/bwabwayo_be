package com.bwabwayo.app.domain.ai.dto.response;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendAiProductResponse {

    private Long id;
    private Category category;
    private String title;
    private int price;
    private List<ProductImage> productImages;
}
