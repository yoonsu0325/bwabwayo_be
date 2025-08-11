package com.bwabwayo.app.domain.product.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id; // 상품 ID
    private Long categoryId; // 카테고리 ID
    private String thumbnail; // 썸네일 URL
    private String title; // 상품명
    private Integer price; // 판매가
    private Integer viewCount; // 조회수
    private Integer wishCount; // 관심수
    private Integer chatCount; // 채팅수
    private Boolean isLike; // 관심 등록 여부
    private Boolean canVideoCall; // 화상 통화 가능 여부
    private Integer saleStatusCode; // 판매 상태 코드
    private String saleStatus; // 판매 상태
    private LocalDateTime createdAt; // 등록 일시
}
