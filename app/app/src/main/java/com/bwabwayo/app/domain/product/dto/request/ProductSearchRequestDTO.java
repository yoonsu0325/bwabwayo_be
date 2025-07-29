package com.bwabwayo.app.domain.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * 상품 검색 조건 요청 DTO
 */
public class ProductSearchRequestDTO {
    private String keyword; // 검색 키워드
    private Long categoryId; // 카테고리 필터링

    @Builder.Default
    private Integer page = 0; // 페이지 번호
    @Builder.Default
    private Integer size = 100; // 상품 수
}
