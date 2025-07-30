package com.bwabwayo.app.domain.product.dto.response;

import com.bwabwayo.app.domain.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 요약 정보 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSimpleDTO {
    private Long id; // 상품 ID
    private Long categoryId; // 카테고리 ID
    private String thumbnail; // 썸네일 URL
    private String title; // 상품명
    private Integer price; // 판매가
    private Integer viewCount; // 조회수
    private Integer wishCount; // 관심수
    private Integer chatCount; // 채팅수
    private Boolean isLike; // 관심 등록 여부
    private Integer saleStatusCode; // 판매 상태 코드
    private String saleStatus; // 판매 상태
    private LocalDateTime createdAt; // 등록 일시


    public static ProductSimpleDTO fromEntity(Product product){
        return ProductSimpleDTO.builder()
                .id(product.getId())
                .categoryId(product.getCategory().getId())
                .thumbnail(product.getThumbnail())
                .title(product.getTitle())
                .price(product.getPrice())
                .viewCount(product.getViewCount())
                .wishCount(product.getWishCount())
                .chatCount(product.getChatCount())
                .isLike(false) // 위시 리스트 미구현
                .saleStatusCode(product.getSaleStatus().getLevel())
                .saleStatus(product.getSaleStatus().getDescription())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
