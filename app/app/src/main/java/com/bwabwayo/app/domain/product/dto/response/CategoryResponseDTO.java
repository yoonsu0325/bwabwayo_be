package com.bwabwayo.app.domain.product.dto.response;

import com.bwabwayo.app.domain.product.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {
    private Long categoryId;
    private String categoryName;
    private List<CategoryResponseDTO> subCategories;

    public static CategoryResponseDTO fromEntity(Category category){
        return CategoryResponseDTO.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .subCategories(
                        category.getChildren() == null
                                ? null
                                : category.getChildren().stream()
                                    .map(CategoryResponseDTO::fromEntity)
                                    .toList()
                )
                .build();
    }
}
