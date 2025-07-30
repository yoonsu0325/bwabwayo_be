package com.bwabwayo.app.domain.product.dto;

import lombok.Getter;

@Getter
public enum ResponseMessage {
    PRODUCT_CREATE_SUCCESS("상품 등록에 성공하였습니다."),

    PRODUCT_SEARCH_SUCCESS("상품 조회에 성공하였습니다."),

    PRODUCT_DETAIL_SUCCESS("상품 상세 정보 조회에 성공하였습니다."),

    PRODUCT_UPDATE_SUCCESS("상품 갱신에 성공하였습니다."),

    PRODUCT_DELETE_SUCCESS("상품을 삭제하였습니다."),

    PRODUCT_NOT_FOUND("상품을 찾을 수 없습니다."),
    PRODUCT_SERVER_ERROR("서버에 오류가 발생하였습니다.");

    private final String text;

    ResponseMessage(String text) {
        this.text = text;
    }
}
