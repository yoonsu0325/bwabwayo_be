package com.bwabwayo.app.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetInvoiceNumberRequest {
    @JsonProperty("courier_code")
    String courierCode;
    @JsonProperty("tracking_number")
    String trackingNumber;
}
