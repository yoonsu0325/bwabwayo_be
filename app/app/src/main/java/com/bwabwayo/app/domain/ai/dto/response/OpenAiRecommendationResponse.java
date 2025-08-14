package com.bwabwayo.app.domain.ai.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OpenAiRecommendationResponse {
    private List<ProductInfo> products;

    @Getter
    @NoArgsConstructor
    public static class ProductInfo {
        private String name;
        private String feature;
        private String priceRange;
        private String advantage;
    }
}
