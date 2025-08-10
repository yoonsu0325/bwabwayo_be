package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.exception.ProductUpdateNotAllowedException;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.product.service.ProductSimilarityService;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.dto.request.ProductUpsertRequest;
import com.bwabwayo.app.domain.product.dto.request.ProductQueryRequest;
import com.bwabwayo.app.domain.product.dto.response.*;
import com.bwabwayo.app.domain.product.dto.response.ProductCreateResponse;
import com.bwabwayo.app.domain.product.dto.response.ProductDetailResponse;
import com.bwabwayo.app.domain.product.dto.response.ProductQueryResponse;
import com.bwabwayo.app.domain.product.dto.response.ViewCountResponse;
import com.bwabwayo.app.global.exception.BadRequestException;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ViewCountService viewCountService;
    private final ProductSimilarityService productSimilarityService;
    private final RestTemplate restTemplate;
    private final ProductRepository productRepository;

    // ========== 생성/수정/삭제 =================

    @Operation(summary = "상품 등록")
    @ApiResponse(responseCode = "200", description = "상품 등록 완료")
    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(
            @Valid @RequestBody ProductUpsertRequest request,
            @LoginUser User loginUser
    ) {
        try{
            // 상품 저장
            Product product = productService.createProduct(request, loginUser);
            // 벡터 추가
            productSimilarityService.upsert(product);
            // Response 생성
            return ResponseEntity.ok(ProductCreateResponse.from(product));
        } catch(IllegalArgumentException e){
            throw new BadRequestException(e.getMessage());
        }
    }

    @Operation(summary = "상품 정보 갱신")
    @ApiResponse(responseCode = "200", description = "상품 정보 갱신 완료")
    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpsertRequest request,
            @LoginUser User loginUser
    ) {
        Product product = productService.findById(productId);
        ensureOwner(loginUser, product);

        try{
            productService.update(product, request);
        } catch(IllegalArgumentException e){
            throw new BadRequestException(e.getMessage());
        }
        
        // 검색 엔진 내 상품 정보 갱신
        productSimilarityService.upsert(product);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "상품 삭제")
    @ApiResponse(responseCode = "200")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteById(@PathVariable Long productId, @LoginUser User loginUser){
        Product product = productService.findById(productId);
        ensureOwner(loginUser, product);

        // 상품 삭제
        productService.delete(product);

        // 검색 엔진에서도 함께 삭제
        productSimilarityService.deleteById(productId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 상품 목록 조회")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductQueryResponse.class))
    )
    @GetMapping("/my")
    public ResponseEntity<?> getMyProductList(@LoginUser User loginUser){
        return getProducts(ProductQueryRequest.builder().sellerId(loginUser.getId()).build(), loginUser);
    }

    @Operation(summary = "상품 목록 조회")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductPageResponse.class))
    )
    @GetMapping
    public ResponseEntity<?> getProducts(
            @Valid @ModelAttribute ProductQueryRequest request,
            @LoginUser(required = false) User user
    ) {
        return ResponseEntity.ok(productService.query(request, user));
    }

    @Operation(summary = "상품 상세 조회")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDetailResponse.class))
    )
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(
            @PathVariable Long productId,
            @LoginUser(required = false) User user
    ) {
        Product product = productService.findById(productId);

        ProductDetailResponse productDetail = productService.getProductDetail(product, user);

        return ResponseEntity.ok(productDetail);
    }

    /* ================ 조회수 ====================*/
    @Operation(summary = "조회수 증가")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ViewCountResponse.class))
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
        return ResponseEntity.ok(new ViewCountResponse(productId, count.intValue()));
    }

    /* ================ 배송 조회 ====================*/
    
    @GetMapping("/deliveryAPI")
    public ResponseEntity<?> foo(@RequestParam String tInvoice, String tCode){
        String url = UriComponentsBuilder.fromUriString("https://info.sweettracker.co.kr/api/v1/trackingInfo")
                .queryParam("t_key", "FezwHddl3sDTP67yZHfbgQ")
                .queryParam("t_code", tCode)
                .queryParam("t_invoice", tInvoice)
                .toUriString();

        ResponseEntity<TrackingInfoResponse> response = restTemplate.getForEntity(url, TrackingInfoResponse.class);
        return  ResponseEntity.ok(response.getBody());
    }

    /* ================ Qdrant ====================*/

    @GetMapping("/embbeding")
    public ResponseEntity<Void> embbeding(){
        List<Product> all = productRepository.findAll();
        for (Product product : all) {
            productSimilarityService.upsert(product);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unembbeding")
    public ResponseEntity<Void> unembbeding(){
        List<Product> all = productRepository.findAll();
        for (Product product : all) {
            productSimilarityService.deleteById(product.getId());
        }
        return ResponseEntity.ok().build();
    }

    // ============ 유틸리티 ===================

    private static void ensureOwner(User loginUser, Product product) {
        if(product.getSeller().equals(loginUser)) {
            throw new ProductUpdateNotAllowedException(product.getId(), loginUser.getId());
        }
    }
}
