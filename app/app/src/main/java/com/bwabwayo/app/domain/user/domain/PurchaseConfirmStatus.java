package com.bwabwayo.app.domain.user.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PurchaseConfirmStatus {
    IN_PROGRESS(0),        // 거래중
    ENABLED(1),            // 구매확정 가능
    CONFIRMED(2),          // 구매확정 완료
    DIRECT(3);             // (예정) 직거래

    private final int code;

    PurchaseConfirmStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PurchaseConfirmStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status code: " + code));
    }
}

