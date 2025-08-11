package com.bwabwayo.app.domain.product.exception;


import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private Long productId;

    public ProductNotFoundException(String message) { super(message);}

    public ProductNotFoundException(Long productId) {
      this("상품을 찾을 수 없습니다: productId="+productId);
      this.productId = productId;
    }
}
