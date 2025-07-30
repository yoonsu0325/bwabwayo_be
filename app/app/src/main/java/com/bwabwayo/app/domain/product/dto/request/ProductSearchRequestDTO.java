package com.bwabwayo.app.domain.product.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

/**
 * 상품 조회 조건 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@ParameterObject
public class ProductSearchRequestDTO {
    private String keyword; // 검색 키워드
    private Long categoryId; // 카테고리 ID

    @Builder.Default
    @Parameter(example = "0")
    private Integer page = 0; // 페이지 번호
    @Builder.Default
    @Parameter(example = "100")
    private Integer size = 100; // 상품 수
}
