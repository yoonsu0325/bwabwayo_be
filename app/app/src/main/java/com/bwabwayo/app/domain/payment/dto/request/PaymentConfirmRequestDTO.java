package com.bwabwayo.app.domain.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequestDTO {
    private String orderId;
    private String paymentKey;
    private Integer amount;
}
