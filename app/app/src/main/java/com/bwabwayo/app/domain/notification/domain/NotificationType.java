package com.bwabwayo.app.domain.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    // 거래 관련
    PRODUCT_SOLD("상품 판매 완료", "상품이 판매 완료되었습니다."),
    PRODUCT_RESERVED("상품 예약 완료", "상품이 예약되었습니다."),
    PRODUCT_UPDATED("상품 정보 변경", "등록한 상품 정보가 수정되었습니다."),
    PRODUCT_DELETED("상품 삭제됨", "상품이 삭제되었습니다."),

    // 관심/찜 관련
    PRODUCT_WISHLISTED("찜 알림", "누군가 내 상품을 찜했습니다."),
    PRODUCT_WISHLISTED_BACK("재등록 알림", "찜한 상품이 다시 등록되었습니다."),

    // 채팅/화상거래 관련
    NEW_CHAT_MESSAGE("새 메시지", "새로운 채팅 메시지가 도착했습니다."),
    VIDEO_CALL_REQUESTED("화상 거래 요청", "화상 거래 요청이 도착했습니다."),

    // 거래 상태
    DELIVERY_STARTED("배송 시작", "상품이 배송을 시작했습니다."),
    DELIVERY_COMPLETED("배송 완료", "상품이 배송 완료되었습니다."),
    TRANSACTION_CONFIRMED("거래 확정", "구매자가 거래를 확정했습니다."),
    TRANSACTION_CANCELED("거래 취소", "거래가 취소되었습니다."),

    // 시스템/관리자
    ADMIN_NOTICE("공지사항", "운영자 공지사항이 도착했습니다."),
    POLICY_VIOLATION_WARNING("정책 위반 경고", "정책 위반으로 경고를 받았습니다.");

    private final String title;
    private final String defaultMessage;
}

