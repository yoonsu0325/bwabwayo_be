package com.bwabwayo.app.domain.chat.domain;

public enum MessageType {
    CREATE_ROOM,

    TEXT,                         // 일반 대화 텍스트
    IMAGE,                       // 이미지 (S3 업로드 URL)

    RESERVE_VIDEOCALL,       // 화상거래 예약
    CANCEL_VIDEOCALL,         // 화상거래 예약 취소
    START_VIDEOCALL,           // 화상거래 시작
    REQUEST_DEPOSIT,        //입금 요청
    START_TRADE,           // 거래 시작 (입금 요청)
    INPUT_DELIVERY_ADDRESS,  // 배송지 입력
    INPUT_TRACKING_NUMBER,    // 송장번호 입력
    START_DELIVERY,     // 배송 시작
    COMPLETE_DELIVERY, // 배송 완료
    CONFIRM_PURCHASE, // 구매 확정 요청

    INPUT_PRICE,            // 가격 입력
    END_TRADE //구매확정  거래 종료!
}
