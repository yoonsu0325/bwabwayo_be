package com.bwabwayo.app.domain.payment.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentConfirmRequest {
    private String orderId;
    private String paymentKey;
    private Integer amount;
    private Long productId;
}
