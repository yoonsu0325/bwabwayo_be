package com.bwabwayo.app.domain.product.exception;

import lombok.Getter;

@Getter
public class ProductUpdateNotAllowedException extends RuntimeException {
    private Long productId; // 업데이트 하려는 상품의 ID
    private String userId; // 업데이트를 시도한 사용자의 ID

    public ProductUpdateNotAllowedException(String message) {
        super(message);
    }

    public ProductUpdateNotAllowedException(Long productId) {
        this(buildMessage(productId, null));
        this.productId = productId;
    }

    public ProductUpdateNotAllowedException(Long productId, String userId) {
        this(buildMessage(productId, userId));
        this.productId = productId;
        this.userId = userId;
    }


    private static String buildMessage(Long productId, String userId) {
        StringBuilder sb = new StringBuilder("상품을 수정/삭제할 권한이 없습니다: productId=")
                .append(productId);
        if (userId != null) {
            sb.append(", userId=").append(userId);
        }
        return sb.toString();
    }
}
