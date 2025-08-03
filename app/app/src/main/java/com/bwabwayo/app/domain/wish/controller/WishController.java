package com.bwabwayo.app.domain.wish.controller;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.exception.NotFoundException;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.user.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.wish.dto.response.ExistsResponseDTO;
import com.bwabwayo.app.domain.wish.service.WishService;
import io.swagger.v3.oas.annotations.Parameter;
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


    @GetMapping
    public ResponseEntity<?> getAllMyWishes(@Parameter(hidden = true) @LoginUser User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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

    @GetMapping("/{productId}")
    public ResponseEntity<?> isWishProduct(
            @PathVariable Long productId,
            @Parameter(hidden = true) @LoginUser User user) {
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