package com.bwabwayo.app.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductQueryResponse {
    private Integer size;
    private List<ProductQueryResult> result; // 조회된 상품 목록

    private Integer start = 1; // 시작 페이지 번혼
    private Integer last; // 끝 페이지 번호

    private Boolean prev; // 이전 존재 여부
    private Boolean next; // 다음 페이지 존재 여부

    private Integer current = 1; // 현재 페이지 번호

    private Integer totalPages; // 총 페이지 수
    private Long totalItems; // 총 항목 수
}
