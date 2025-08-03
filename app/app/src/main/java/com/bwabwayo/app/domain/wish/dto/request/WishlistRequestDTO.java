package com.bwabwayo.app.domain.wish.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ParameterObject
public class WishlistRequestDTO {
    @Builder.Default
    @Min(1)
    private Integer pageNo = 1;
    @Builder.Default
    @Min(0)
    private Integer pageSize = 100;
}
