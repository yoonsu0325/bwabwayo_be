package com.bwabwayo.app.domain.ai.dto.response;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.ProductImage;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class RecommendAiProductResponse {
    private Long id;
    private Long categoryId;     // ← 엔티티 대신 id/name만
    private String categoryName;
    private String title;
    private int price;
    private List<String> imageUrls; // ← 이미지도 URL만
}
