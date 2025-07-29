package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.ErrorResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductSearchResponseDTO;
import com.bwabwayo.app.domain.product.service.ProductService;
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
     * 상품 전체 조회
     */
    @GetMapping
    public ResponseEntity<?>getProducts(@ModelAttribute ProductSearchRequestDTO requestDTO) {
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
