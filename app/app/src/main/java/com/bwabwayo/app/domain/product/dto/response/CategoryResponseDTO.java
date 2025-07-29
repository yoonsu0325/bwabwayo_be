package com.bwabwayo.app.domain.product.dto.response;

import com.bwabwayo.app.domain.product.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;
    @Schema(description = "카테고리 이름", example = "전자제품")
    private String categoryName;
    @Schema(
            description = "하위 카테고리 목록",
            example = "[{\"categoryId\":2,\"categoryName\":\"소형가전\",\"subCategories\":[{\"categoryId\":3,\"categoryName\":\"전자레인지\",\"subCategories\":[]}]}]"
    )
    private List<CategoryResponseDTO> subCategories;


    public static CategoryResponseDTO fromEntity(Category category, int depth){
        // 순환 참조 방지를 위한 접근 깊이 제한
        if(depth <= 0 || category == null) return null;

        return CategoryResponseDTO.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .subCategories(
                        category.getChildren() == null
                                ? List.of()
                                : category.getChildren().stream()
                                    .map(c -> CategoryResponseDTO.fromEntity(c, depth - 1))
                                    .filter(Objects::nonNull)
                                    .toList()
                )
                .build();
    }
}
