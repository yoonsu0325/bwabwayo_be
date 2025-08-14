package com.bwabwayo.app.domain.payment.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentConfirmRequest {
    private String orderId; // 주문 ID (직접 생성)
    private String paymentKey; // 결제 키 (토스 발급)
    private Integer amount; // 거래 금액
//    private Long productId; // 거래 상품
    private Long roomId; // 거래 상품
}
