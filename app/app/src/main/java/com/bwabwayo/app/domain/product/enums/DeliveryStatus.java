package com.bwabwayo.app.domain.product.enums;

import lombok.Getter;

@Getter
public enum DeliveryStatus {
    DIRECT(0, "직거래"),
    PREPARING(1, "배송준비중"),
    COLLECTED(2, "집화완료"),
    IN_TRANSIT(3, "배송중"),
    ARRIVED_AT_BRANCH(4, "지점 도착"),
    OUT_FOR_DELIVERY(5, "배송출발"),
    DELIVERED(6, "배송 완료");

    private final int level;
    private final String description;

    DeliveryStatus(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public static DeliveryStatus fromLevel(int level) {
        for (DeliveryStatus status : values()) {
            if (status.level == level) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown delivery level: " + level);
    }
}
