package com.bwabwayo.app.domain.chat.dto.request;

import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetProductStatusRequest {
    @JsonProperty("product_status")
    SaleStatus productStatus;
}
