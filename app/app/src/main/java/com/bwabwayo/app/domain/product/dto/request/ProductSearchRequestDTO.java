package com.bwabwayo.app.domain.product.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
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

    @Parameter(example = "1")
    @Min(1)
    @Builder.Default
    private Integer page = 1; // 페이지 번호

    @Parameter(example = "100")
    @Min(0)
    @Builder.Default
    private Integer size = 100; // 상품 수

    @Parameter(example = "latest")
    @Builder.Default
    private String sortBy = "latest"; // 정렬 기준

    private String sellerId; // 판매자 ID

    private Boolean canVideoCall;
    private Boolean canNegotiate;
    private Boolean canDirect;
    private Boolean canDelivery;
}
