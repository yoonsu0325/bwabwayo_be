package com.bwabwayo.app.domain.product.enums;

import java.util.Arrays;

public enum PaymentStatus {

    PENDING("PENDING", "결제 대기"),
    COMPLETED("COMPLETED", "결제 완료"),
    FAILED("FAILED", "결제 실패"),
    CANCELED("CANCELED", "결제 취소"),
    REFUNDED("REFUNDED", "결제 환불");

    private final String code;
    private final String description;

    PaymentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * PG사 상태 코드로부터 PaymentStatus 변환
     * 예: Toss, 카카오페이 등의 응답 상태 값
     */
    public static PaymentStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown PaymentStatus code: " + code)
                );
    }
}

