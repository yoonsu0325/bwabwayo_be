package com.bwabwayo.app.domain.product.enums;

import lombok.Getter;

@Getter
public enum SaleStatus {

    AVAILABLE(0, "판매중"),
    NEGOTIATING(1, "거래중"),
    SOLD_OUT(2, "판매완료");

    private final int level;
    private final String description;

    SaleStatus(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public static SaleStatus fromLevel(int level) {
        for (SaleStatus status : values()) {
            if (status.level == level) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown product status level: " + level);
    }
}
