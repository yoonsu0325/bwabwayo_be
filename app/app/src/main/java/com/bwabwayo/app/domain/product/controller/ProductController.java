package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.ai.service.ProductSimilarityService;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.dto.request.ProductCreateAndUpdateRequestDTO;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductCreateResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductDetailResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductSearchResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ViewCountResponseDTO;
import com.bwabwayo.app.global.exception.BadRequestException;
import com.bwabwayo.app.global.exception.ForbiddenException;
import com.bwabwayo.app.global.exception.NotFoundException;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.product.service.ViewCountService;
import com.bwabwayo.app.domain.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
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
    private final ProductSimilarityService productSimilarityService;


    @Operation(summary = "내 상품 목록 조회")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductSearchResponseDTO.class))
    )
    @GetMapping("/my")
    public ResponseEntity<?> getMyProductList(@LoginUser User loginUser){
        return getProducts(ProductSearchRequestDTO.builder().sellerId(loginUser.getId()).build(), loginUser);
    }

    @Operation(summary = "상품 등록")
    @ApiResponse(responseCode = "200",
            description = "상품 등록 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductCreateResponseDTO.class))
    )
    @PostMapping
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductCreateAndUpdateRequestDTO requestDTO,
            @LoginUser User user
    ) {
        try{
            // 상품 저장
            Product product = productService.createProduct(requestDTO, user);
            // 벡터 추가
            productSimilarityService.savePoint(product);
            // Response 생성
            return ResponseEntity.ok(ProductCreateResponseDTO.fromEntity(product));
        } catch(IllegalArgumentException e){
            throw new BadRequestException(e.getMessage());
        }
    }

    @Operation(summary = "상품 목록 조회")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductSearchResponseDTO.class))
    )
    @GetMapping
    public ResponseEntity<?> getProducts(
            @Valid @ModelAttribute ProductSearchRequestDTO requestDTO,
            @LoginUser(required = false) User user
    ) {
        ProductSearchResponseDTO response = productService.searchProducts(requestDTO, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 상세 조회")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDetailResponseDTO.class))
    )
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(
            @PathVariable Long productId,
            @LoginUser(required = false) User user
    ) {
        Product product = validateProduct(productId);

        ProductDetailResponseDTO productDetail = productService.getProductDetail(product, user);

        return ResponseEntity.ok(productDetail);
    }

    @Operation(summary = "상품 정보 갱신")
    @ApiResponse(responseCode = "200")
    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductCreateAndUpdateRequestDTO requestDTO,
            @LoginUser User loginUser
    ) {
        Product product = validateProduct(productId, loginUser);
        try{
            productService.update(product, requestDTO);
        } catch(IllegalArgumentException e){
            throw new BadRequestException(e.getMessage());
        }
        // 벡터 갱신
        productSimilarityService.deletePoint(productId);
        productSimilarityService.savePoint(product);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "상품 삭제")
    @ApiResponse(responseCode = "200")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductById(
            @PathVariable Long productId,
            @LoginUser User loginUser
    ){
        Product product = validateProduct(productId, loginUser);
        // 상품 삭제
        productService.delete(product);
        // 벡터 삭제
        productSimilarityService.deletePoint(productId);
        
        return ResponseEntity.ok().build();
    }

    private Product validateProduct(Long productId) {
        Product product;
        try{
            product = productService.findById(productId);
        } catch (IllegalArgumentException e){
            log.info("상품을 찾을 수 없음: productId={}", productId);
            throw new NotFoundException("상품을 찾을 수 없습니다.");
        }
        return product;
    }

    private Product validateProduct(Long productId, User loginUser) {
        Product product = validateProduct(productId);

        User seller = product.getSeller();
        if(!seller.getId().equals(loginUser.getId())){
            log.info("권한 없음: seller={}, loginUser={}", seller.getId(), loginUser.getId());
            throw new ForbiddenException("상품의 상태를 변경할 권한이 없습니다.");
        }

        return product;
    }

    // =================
    // 조회수
    // =================
    @Operation(summary = "조회수 증가")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ViewCountResponseDTO.class))
    )
    @GetMapping("{productId}/view")
    public ResponseEntity<?> increaseViewCount(
            @PathVariable Long productId,
            HttpServletRequest request,
            @LoginUser(required = false) User loginUser
    ){
        // 비로그인 사용자는 IP 기준으로 식별
        String identifier = loginUser != null ? loginUser.getId() : request.getRemoteAddr();

        Long count = viewCountService.increaseViewCount(productId, identifier);
        return ResponseEntity.ok(new ViewCountResponseDTO(productId, count.intValue()));
    }
}
