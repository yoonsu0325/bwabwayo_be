package com.bwabwayo.app.domain.product.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingInfoResponse {
    private boolean status;
    private List<TrackingDetail> trackingDetails;
}
