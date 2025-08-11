package com.bwabwayo.app.domain.product.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductQueryCondition {
    private String viewerId;

    private String keyword;
    private Long categoryId;
    private List<Long> categoryIn;

    private String sellerId;

    private Boolean canVideoCall;
    private Boolean canNegotiate;
    private Boolean canDelivery;
    private Boolean canDirect;

    private Integer minPrice;
    private Integer maxPrice;

    private String urlPrefix;
}
