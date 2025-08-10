package com.bwabwayo.app.domain.wish.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishDTO {
    private Long id;

    private Long productId;

    private Long categoryId;
    private String categoryName;

    private String thumbnail;
    private String title;
    private Integer price;

    private Integer viewCount;
    private Integer wishCount;
    private Integer chatCount;
    private boolean canVideoCall;

    private Integer saleStatusCode;
    private String saleStatus;

    private LocalDateTime createdAt;
}
