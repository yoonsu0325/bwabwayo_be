package com.bwabwayo.app.domain.product.dto;

import lombok.Getter;

@Getter
public enum ResponseMessage {
    PRODUCT_CREATE_SUCCESS("상품 등록에 성공하였습니다."),
    PRODUCT_CREATE_FAIL("상품 등록 중 서버에 오류가 발생하였습니다."),

    PRODUCT_SEARCH_FAIL("상품 조회 중 서버에 오류가 발생하였습니다."),

    PRODUCT_DETAIL_NOT_FOUND("조회하려는 상품을 찾을 수 없습니다."),
    PRODUCT_DETAIL_FAIL("상품 조회 중 서버에서 오류가 발생하였습니다."),

    PRODUCT_DELETE_SUCCESS("상품을 삭제하였습니다."),
    PRODUCT_DELETE_NOT_FOUND("삭제하려는 상품을 찾을 수 없습니다."),
    PRODUCT_DELETE_FAIL("서버에서 오류가 발생하였습니다.");

    private final String text;

    ResponseMessage(String text) {
        this.text = text;
    }
}
