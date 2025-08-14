package com.bwabwayo.app.domain.product.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingDetail {
    private String timeString;
    private String where;
    private String kind;
    private int level;
}