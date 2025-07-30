package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.ErrorResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductSearchResponseDTO;
import com.bwabwayo.app.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    /**
     * 상품 목록 조회
     */
    @Operation(summary = "상품 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "상품 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductSearchResponseDTO.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping
    public ResponseEntity<?>getProducts(@ModelAttribute ProductSearchRequestDTO requestDTO) {
        // 유효하지 않다면, 기본값으로 초기화
        if(requestDTO.getPage() < 1) requestDTO.setPage(1);
        if(requestDTO.getSize() < 0) requestDTO.setSize(100);

        try{
            ProductSearchResponseDTO response = productService.searchProducts(requestDTO);

            return ResponseEntity.ok(response);
        } catch(Exception e){
            log.error("상품 조회 중 오류 발생", e);

            ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                    .message("상품 조회 중 서버에 오류가 발생하였습니다.")
                    .build();
            return ResponseEntity.status(500).body(errorResponseDTO);
        }
    }
}
