package com.bwabwayo.app.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.List;

/**
 * 상품 조회 목록 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchResponseDTO {
    private String message;
    private List<ProductSearchResultDTO> result; // 조회된 상품 목록

    private Integer start, last; // 시작, 끝 페이지 번호
    private Boolean prev, next; // 이전, 다음 페이지 존재 여부
    private Integer current; // 현재 페이지 번호
    private Integer totalPages; // 총 페이지 수
    private Long totalItems; // 총 항목 수
}
