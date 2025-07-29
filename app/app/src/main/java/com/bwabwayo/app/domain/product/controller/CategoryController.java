package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.dto.response.CategoryListResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.CategoryResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ErrorResponseDTO;
import com.bwabwayo.app.domain.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/categories")
@RequiredArgsConstructor
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;

    /**
     * 카테고리 목록 조회
     */
    @Operation(summary = "카테고리 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "카테고리 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryListResponseDTO.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping
    public ResponseEntity<?> getTopCategories() {
        try{
            // 최상위 카테고리 조회
            List<Category> topCategories = categoryService.getTopCategories();

            // DTO로 매핑
            List<CategoryResponseDTO> categories =
                    topCategories.stream()
                            .map(c -> CategoryResponseDTO.fromEntity(c, 3))
                            .toList();

            // Response 생성
            CategoryListResponseDTO response = CategoryListResponseDTO.builder()
                    .message("카테고리 조회에 성공했습니다.")
                    .categories(categories)
                    .build();

            return ResponseEntity.ok(response);
        } catch(Exception e){
            // 서버에 오류 발생
            log.error("카테고리 조회 중 오류 발생", e);

            ErrorResponseDTO response = ErrorResponseDTO.builder()
                    .message("카테고리 조회 중 서버 오류가 발생했습니다.")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
