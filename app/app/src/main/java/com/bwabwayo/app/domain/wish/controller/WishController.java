package com.bwabwayo.app.domain.wish.controller;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.wish.dto.request.WishlistRequest;
import com.bwabwayo.app.domain.wish.dto.response.WishExistsResponse;
import com.bwabwayo.app.domain.wish.dto.WishDTO;
import com.bwabwayo.app.domain.wish.dto.response.WishPageResponse;
import com.bwabwayo.app.domain.wish.service.WishService;
import com.bwabwayo.app.global.page.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/wishes")
public class WishController {

    private final WishService wishService;
    private final ProductService productService;

    
    @Operation(summary = "내 위시리스트 조회")
    @ApiResponse(
            responseCode = "200"
            , description = "위시리스트 조회 성공"
            , content = @Content(mediaType = "application/json", schema = @Schema(implementation = WishPageResponse.class))
    )
    @GetMapping
    public ResponseEntity<?> getAllMyWishes(
            @Valid @ModelAttribute WishlistRequest request,
            @LoginUser User loginUser
    ) {
        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();

        PageResponse<WishDTO> responseDTO = wishService.getAllMyWishes(loginUser, pageNo, pageSize);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "내 위시리스트에 추가")
    @ApiResponse(responseCode = "200", description = "위시리스트에 추가 성공")
    @PostMapping("/{productId}")
    public ResponseEntity<Void> addWishProduct(@PathVariable Long productId, @LoginUser User loginUser) {
        Product product = productService.findById(productId);

        wishService.add(product, loginUser);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 위시리스트에서 삭제")
    @ApiResponse(responseCode = "200", description = "위시리스트에서 삭제 성공")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeWishProduct(@PathVariable Long productId, @LoginUser User user) {
        Product product = productService.findById(productId);

        wishService.delete(product, user);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 위시리스트에 등록 여부 조회")
    @ApiResponse(responseCode = "200", description = "내 위시리스트 등록 여부 조회 성공")
    @GetMapping("/{productId}")
    public ResponseEntity<WishExistsResponse> isWishProduct(@PathVariable Long productId, @LoginUser User loginUser) {
        Product product = productService.findById(productId);

        boolean exists = wishService.existsWish(product, loginUser);

        return ResponseEntity.ok(WishExistsResponse.from(exists));
    }
}