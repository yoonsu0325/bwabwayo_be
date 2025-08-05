package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.dto.request.ProductCreateAndUpdateRequestDTO;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductCreateResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductDetailResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductSearchResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ViewCountResponseDTO;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.product.service.ViewCountService;
import com.bwabwayo.app.domain.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ViewCountService viewCountService;

    @Operation(summary = "상품 등록")
    @ApiResponse(
            responseCode = "200",
            description = "상품 등록 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductCreateResponseDTO.class))
    )
    @PostMapping
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductCreateAndUpdateRequestDTO requestDTO,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        ProductCreateResponseDTO responseDTO = productService.createProduct(requestDTO, user);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "상품 목록 조회")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductSearchResponseDTO.class))
    )
    @GetMapping
    public ResponseEntity<?> getProducts(
            @Valid @ModelAttribute ProductSearchRequestDTO requestDTO,
            @Parameter(hidden = true) @LoginUser(required = false) User user
    ) {
        ProductSearchResponseDTO response = productService.searchProducts(requestDTO, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 상세 조회")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDetailResponseDTO.class))
    )
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(
            @PathVariable Long productId,
            @Parameter(hidden = true) @LoginUser(required = false) User user
    ) {
        ProductDetailResponseDTO productDetail = productService.getProductDetail(productId, user);
        return ResponseEntity.ok(productDetail);
    }

    @Operation(summary = "상품 정보 갱신")
    @ApiResponse(responseCode = "200")
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductCreateAndUpdateRequestDTO requestDTO,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        productService.updateProduct(productId, requestDTO, user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "상품 삭제")
    @ApiResponse(responseCode = "200")
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProductById(
            @PathVariable Long productId,
            @Parameter(hidden = true) @LoginUser User user
    ){
        productService.deleteProductById(productId, user);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "조회수 증가")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ViewCountResponseDTO.class))
    )
    @GetMapping("{productId}/view")
    public ResponseEntity<?> increaseViewCount(
            @PathVariable Long productId,
            HttpServletRequest request,
            @Parameter(hidden = true) @LoginUser(required = false) User loginUser
    ){
        // 비로그인 사용자는 IP 기준으로 식별
        String identifier = loginUser != null ? loginUser.getId() : request.getRemoteAddr();

        Long count = viewCountService.increaseViewCount(productId, identifier);
        return ResponseEntity.ok(new ViewCountResponseDTO(productId, count.intValue()));
    }
}
