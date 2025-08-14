package com.bwabwayo.app.domain.user.domain;

public enum PointEventType {

    // 포인트 수입처
    SIGNUP_FIRST(3000, "첫 회원가입"),
    DEAL_COMPLETED(1, "거래 성사"), // 동적 계산 필요
    ATTENDANCE(100, "하루 출석"),
    REVIEW_WRITTEN(200, "리뷰 작성"),

    // 포인트 사용처
    VIDEO_CALL(-1000, "화상 회의 1회 사용"),
    ITEM_PURCHASE(-2000, "물품 구매"); // 동적 계산 가능

    private final int point;
    private final String description;

    PointEventType(int point, String description) {
        this.point = point;
        this.description = description;
    }

    public int getPoint() {
        return point;
    }

    public String getDescription() {
        return description;
    }

    public boolean isIncome() {
        return this.point > 0;
    }

    public boolean isExpense() {
        return this.point < 0;
    }

    public boolean isDynamic() {
        return this == DEAL_COMPLETED || this == ITEM_PURCHASE;
    }
}

