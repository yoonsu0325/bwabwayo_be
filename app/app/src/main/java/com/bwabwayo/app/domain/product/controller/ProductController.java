package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.dto.request.ProductCreateRequestDTO;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.MessageDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductCreateResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductSearchResponseDTO;
import com.bwabwayo.app.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록")
    @PostMapping
    private ResponseEntity<?> createProduct(@ModelAttribute ProductCreateRequestDTO requestDTO){
        try{
            ProductCreateResponseDTO responseDTO = productService.createProduct(requestDTO);
            return ResponseEntity.ok(responseDTO);
        } catch(Exception e){
            return ResponseEntity.status(500).body(new MessageDTO("상품 등록 중 서버에 오류가 발생하였습니다."));
        }
    }

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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageDTO.class))
            )
    })
    @GetMapping
    public ResponseEntity<?> getProducts(@ModelAttribute ProductSearchRequestDTO requestDTO) {
        // 유효하지 않다면, 기본값으로 초기화
        if(requestDTO.getPage() < 1) requestDTO.setPage(1);
        if(requestDTO.getSize() < 0) requestDTO.setSize(100);

        try{
            ProductSearchResponseDTO response = productService.searchProducts(requestDTO);

            return ResponseEntity.ok(response);
        } catch(Exception e){
            return ResponseEntity.status(500).body(new MessageDTO("상품 조회 중 서버에 오류가 발생하였습니다."));
        }
    }

    @Operation(summary = "상품 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상품"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<MessageDTO> deleteById(@PathVariable Long productId){
        try{
            productService.deleteProduct(productId);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(404).body(new MessageDTO("삭제하려는 상품을 찾을 수 없습니다."));
        } catch (Exception e){
            return ResponseEntity.status(500).body(new MessageDTO("서버에서 오류가 발생하였습니다."));
        }
        return ResponseEntity.ok(new MessageDTO("상품을 삭제하였습니다."));
    }
}
