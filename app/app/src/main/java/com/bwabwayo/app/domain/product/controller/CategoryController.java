package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.dto.response.CategoryListResponse;
import com.bwabwayo.app.domain.product.dto.response.CategoryDTO;
import com.bwabwayo.app.domain.product.dto.response.CategoryResponse;
import com.bwabwayo.app.domain.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/products/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회")
    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    @GetMapping
    public ResponseEntity<CategoryListResponse> getTopCategories() {
        // 최상위 카테고리 조회
        CategoryListResponse response = categoryService.getTopCategories();
        // Response 생성
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리 조회")
    @ApiResponse(responseCode = "200", description = "카테고리 조회 성공")
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getTopCategories(@PathVariable Long categoryId) {
        Category category = categoryService.findById(categoryId);
        
        // 카테고리가 존재하지 않음
        if(category == null){
            log.warn("조회하려는 카테고리가 존재하지 않음: categoryId={}", categoryId);
            return ResponseEntity.notFound().build();
        }
        
        // Response 생성
        List<CategoryDTO> subCategories = new ArrayList<>();
        category.getChildren().forEach(c-> subCategories.add(new CategoryDTO(c.getId(), c.getName())));

        CategoryResponse response = CategoryResponse
                .builder()
                .id(category.getId())
                .name(category.getName())
                .subCategories(subCategories)
                .size(subCategories.size())
                .build();

        return ResponseEntity.ok(response);
    }
}
