package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.dto.response.CategoryListResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.CategoryTreeDTO;
import com.bwabwayo.app.domain.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "카테고리 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryListResponseDTO.class))
            ),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping
    public ResponseEntity<?> getTopCategories() {
        // 최상위 카테고리 조회
        List<CategoryTreeDTO> topCategories = categoryService.getTopCategories();

        // Response 생성
        CategoryListResponseDTO response = CategoryListResponseDTO.builder()
                .message("카테고리 조회에 성공했습니다.")
                .size(topCategories.size())
                .categories(topCategories)
                .build();

        return ResponseEntity.ok(response);
    }
}
