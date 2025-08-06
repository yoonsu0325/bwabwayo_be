package com.bwabwayo.app.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponseDTO {
    private String title;
    private String description;
    private Integer price;

    private Integer saleStatus;

    private Integer shippingFee;

    private Boolean canNegotiate;
    private Boolean canDirect;
    private Boolean canDelivery;
    private Boolean canVideoCall;

    private Boolean isLike;

    private Integer viewCount;
    private Integer wishCount;
    private Integer chatCount;

    private LocalDateTime createdAt;

    @Builder.Default
    private List<CategoryDTO> categories = new ArrayList<>();

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Builder.Default
    private List<String> imageKeys = new ArrayList<>();

    private SellerDTO seller;

    @Builder.Default
    private List<ProductSimpleDTO> similarities = new ArrayList<>();
}
