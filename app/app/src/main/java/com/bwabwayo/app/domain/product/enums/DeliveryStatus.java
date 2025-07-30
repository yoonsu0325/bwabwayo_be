package com.bwabwayo.app.domain.product.enums;

import lombok.Getter;

@Getter
public enum DeliveryStatus {
    DIRECT( -1, "직거래"),
    RECEIVED(0, "상품인수"),
    IN_TRANSIT(1, "상품이동중"),
    AT_HUB(2, "배송지도착"),
    OUT_FOR_DELIVERY(3, "배송출발"),
    DELIVERED(4, "배송완료");

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
