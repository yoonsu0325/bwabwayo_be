package com.bwabwayo.app.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetPriceRequest {
    @JsonProperty("price")
    Integer price;
}
