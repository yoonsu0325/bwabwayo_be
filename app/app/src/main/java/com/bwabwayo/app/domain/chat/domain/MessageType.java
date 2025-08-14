package com.bwabwayo.app.domain.chat.domain;

public enum MessageType {
    TEXT,                         // 일반 대화 텍스트
    IMAGE,                       // 이미지 (S3 업로드 URL)
    CREATE_ROOM,        // 방 생성 시 전송
    RESERVE_VIDEOCALL,       // 화상거래 예약 후 전송
    CANCEL_VIDEOCALL,         // 화상거래 예약 취소 후 전송
    START_VIDEOCALL,           // 화상거래 시작 후 전송
    START_TRADE,           // 거래 시작 - 거래 시작 버튼 클릭 시 전송
    REQUEST_DEPOSIT,        // 입금 요청 - 최종 가격 설정 후 전송
    INPUT_DELIVERY_ADDRESS,  // 배송지 입력 - 입금 완료 후 전송
    INPUT_TRACKING_NUMBER,    // 송장번호 입력 - 배송지 입력 완료 후 전송
    START_DELIVERY,     // 배송 시작 - 송장번호 입력 후 전송
    CONFIRM_PURCHASE, // 구매 확정 요청 - 송장번호 입력 후 전송
    END_TRADE // 구매확정 거래 종료 - 구매 확정 후 전송
}
