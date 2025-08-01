package com.bwabwayo.app.domain.product.dto.response;

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
    private String message;
    private int size;
    private List<CategoryTreeDTO> categories;
}
