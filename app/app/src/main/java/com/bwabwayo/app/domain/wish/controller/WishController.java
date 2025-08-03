package com.bwabwayo.app.domain.wish.controller;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.exception.NotFoundException;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.user.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.wish.dto.request.WishlistRequestDTO;
import com.bwabwayo.app.domain.wish.dto.response.ExistsResponseDTO;
import com.bwabwayo.app.domain.wish.dto.response.WishlistResponseDTO;
import com.bwabwayo.app.domain.wish.service.WishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
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
            responseCode = "200",
            description = "위시리스트 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WishlistResponseDTO.class))
    )
    @GetMapping
    public ResponseEntity<?> getAllMyWishes(
            @Valid @ModelAttribute WishlistRequestDTO requestDTO,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        int pageNo = requestDTO.getPageNo();
        int pageSize = requestDTO.getPageSize();

        WishlistResponseDTO responseDTO = wishService.getAllMyWishes(user, pageNo, pageSize);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "내 위시리스트에 추가")
    @ApiResponse(responseCode = "200", description = "위시리스트에 추가 성공")
    @PostMapping("/{productId}")
    public ResponseEntity<Void> addWishProduct(
            @PathVariable Long productId,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        Product product = productService.getProductById(productId);
        if(product == null){
            log.warn("찜하려는 상품이 존재하지 않음: productId={}",productId);
            throw new NotFoundException("찜하려는 상품이 존재하지 않음: productId="+productId);
        }

        wishService.addWish(product, user);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 위시리스트에서 삭제")
    @ApiResponse(responseCode = "200", description = "위시리스트에서 삭제 성공")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeWishProduct(
            @PathVariable Long productId,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        Product product = productService.getProductById(productId);

        if(product == null){
            log.warn("찜 해제하려는 상품이 존재하지 않음: productId={}",productId);
            throw new NotFoundException("찜 해제하려는 상품이 존재하지 않음: productId="+productId);
        }

        wishService.removeWish(product, user);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 위시리스트에 등록 여부 조회")
    @ApiResponse(
            responseCode = "200"
            , description = "내 위시리스트 등록 여부 조회 성공"
            , content = @Content(mediaType = "application/json",  schema = @Schema(implementation = ExistsResponseDTO.class))
    )
    @GetMapping("/{productId}")
    public ResponseEntity<?> isWishProduct(
            @PathVariable Long productId,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        Product product = productService.getProductById(productId);

        if(product == null){
            log.warn("찜 여부를 확인하려는 상품이 존재하지 않음: productId={}",productId);
            throw new NotFoundException("찜 여부를 확인하려는 상품이 존재하지 않음: productId="+productId);
        }

        boolean exists = wishService.existsWish(product, user);

        ExistsResponseDTO responseDTO = ExistsResponseDTO.builder().exists(exists).build();

        return ResponseEntity.ok(responseDTO);
    }
}