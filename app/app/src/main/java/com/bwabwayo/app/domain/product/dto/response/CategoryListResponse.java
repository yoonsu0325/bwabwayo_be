package com.bwabwayo.app.domain.product.dto.response;

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
public class CategoryListResponse {
    private int totalCategories;
    private int totalTopCategories;
    @Builder.Default
    private List<CategoryTreeDTO> categories = new ArrayList<>();
}
