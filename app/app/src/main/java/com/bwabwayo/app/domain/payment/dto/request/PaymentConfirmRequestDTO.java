package com.bwabwayo.app.domain.payment.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentConfirmRequestDTO {
    private String orderId;
    private String paymentKey;
    private Integer amount;
}
