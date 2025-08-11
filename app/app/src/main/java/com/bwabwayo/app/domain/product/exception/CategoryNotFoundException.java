package com.bwabwayo.app.domain.product.exception;

import lombok.Getter;

@Getter
public class CategoryNotFoundException extends RuntimeException {
    private Long categoryId;

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(Long categoryId){
        this("카테고리를 찾을 수 없습니다: categoryId="+categoryId);
        this.categoryId = categoryId;
    }
}
