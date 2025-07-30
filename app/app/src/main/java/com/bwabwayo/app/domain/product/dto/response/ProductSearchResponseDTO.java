package com.bwabwayo.app.domain.product.dto.response;

import com.bwabwayo.app.domain.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;


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

    public static ProductSearchResponseDTO fromEntity(Page<Product> pageData) {
        List<Product> content = pageData.getContent();

        List<ProductSearchResultDTO> result = content.stream().map(p ->
                ProductSearchResultDTO.builder()
                        .product(ProductSimpleDTO.fromEntity(p))
                        .seller(UserSimpleDTO.fromEntity(p.getSeller()))
                        .build()
        ).toList();

        int current = pageData.getNumber() + 1;
        int end = (int) Math.ceil(current / 10.0) * 10; // 마지막 페이지 블록
        int start = end - 9; // 처음 페이지 블록
        int last = Math.min(end, pageData.getTotalPages()); // 실제 마지막 페이지 블록


        return ProductSearchResponseDTO.builder()
                .message("상품 조회에 성공하였습니다.")
                .result(result)
                .start(start)
                .last(last)
                .prev(current > 1)
                .next(pageData.hasNext())
                .current(current)
                .totalPages(pageData.getTotalPages())
                .totalItems(pageData.getTotalElements())
                .build();
    }
}
