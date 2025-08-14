package com.bwabwayo.app.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTreeDTO {
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;
    @Schema(description = "카테고리 이름", example = "전자제품")
    private String categoryName;
    @Schema(
            description = "하위 카테고리 목록",
            example = "[{\"categoryId\":2,\"categoryName\":\"소형가전\",\"subCategories\":[{\"categoryId\":3,\"categoryName\":\"전자레인지\",\"subCategories\":[]}]}]"
    )
    @Builder.Default
    private List<CategoryTreeDTO> subCategories = new ArrayList<>();
}
