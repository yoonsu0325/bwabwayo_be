package com.bwabwayo.app.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {
    @Schema(example = "카테고리 조회 중 서버 오류가 발생했습니다.")
    private String message;
}
