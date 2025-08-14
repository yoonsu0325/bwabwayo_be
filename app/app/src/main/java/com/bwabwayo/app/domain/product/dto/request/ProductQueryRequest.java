package com.bwabwayo.app.domain.product.dto.request;

import com.bwabwayo.app.domain.product.enums.ProductSortType;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@ParameterObject
public class ProductQueryRequest {
    @Parameter(example = "1")
    @Min(1)
    @Builder.Default
    private Integer page = 1; // 페이지 번호

    @Parameter(example = "100")
    @Min(0)
    @Builder.Default
    private Integer size = 100; // 상품 수

    private String keyword; // 검색 키워드
    private Long categoryId; // 카테고리 ID

    @Parameter(example = "latest", description = "latest(최신순), views(조회수 순), wishes(찜 순), related(관련순: 키워드 필수)")
    @Builder.Default
    private String sortBy = ProductSortType.LATEST.getQueryValue(); // 정렬 기준

    private String sellerId; // 판매자 ID

    private Boolean canVideoCall;
    private Boolean canNegotiate;
    private Boolean canDirect;
    private Boolean canDelivery;

    private Integer minPrice;
    private Integer maxPrice;

    @Parameter(hidden = true)
    private String urlPrefix;

    private Boolean onlySale;
}
