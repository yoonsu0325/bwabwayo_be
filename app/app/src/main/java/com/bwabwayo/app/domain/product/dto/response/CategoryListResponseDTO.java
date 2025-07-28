package com.bwabwayo.app.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryListResponseDTO {
    @Schema(example = "카테고리 조회에 성공했습니다.")
    private String message;
    private List<CategoryResponseDTO> categories;
}
