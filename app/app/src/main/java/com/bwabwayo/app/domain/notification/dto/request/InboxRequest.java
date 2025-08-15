package com.bwabwayo.app.domain.notification.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InboxRequest {
    @Min(0)
    @Builder.Default
    Integer limit = 3;
}
