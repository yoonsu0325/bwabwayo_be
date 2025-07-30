package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.dto.ResponseMessage;
import com.bwabwayo.app.domain.product.dto.request.ProductCreateRequestDTO;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.MessageDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductCreateResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductDetailResponseDTO;
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
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "상품 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductCreateResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류로 상품 등록 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            )
    })
    @PostMapping
    private ResponseEntity<?> createProduct(@ModelAttribute ProductCreateRequestDTO requestDTO){
        try{
            ProductCreateResponseDTO responseDTO = productService.createProduct(requestDTO);
            return ResponseEntity.ok(responseDTO);
        } catch(Exception e){
            return ResponseEntity.status(500).body(new MessageDTO(ResponseMessage.PRODUCT_CREATE_FAIL.getText()));
        }
    }

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
        if(requestDTO.getPage() < 1) requestDTO.setPage(1);
        if(requestDTO.getSize() < 0) requestDTO.setSize(100);

        try{
            ProductSearchResponseDTO response = productService.searchProducts(requestDTO);
            return ResponseEntity.ok(response);
        } catch(Exception e){
            return ResponseEntity.status(500).body(new MessageDTO(ResponseMessage.PRODUCT_SEARCH_FAIL.getText()));
        }
    }

    @Operation(summary = "상품 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 상세 정보 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDetailResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "상품이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageDTO.class)))
    })
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId){
        try{
            ProductDetailResponseDTO productDetail = productService.getProductDetail(productId);
            return ResponseEntity.ok(productDetail);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(404).body(new MessageDTO(ResponseMessage.PRODUCT_DETAIL_NOT_FOUND.getText()));
        } catch(Exception e){
            return ResponseEntity.status(500).body(new MessageDTO(ResponseMessage.PRODUCT_DETAIL_FAIL.getText()));
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
    public ResponseEntity<MessageDTO> deleteProductById(@PathVariable Long productId){
        try{
            productService.deleteProductById(productId);
            return ResponseEntity.ok(new MessageDTO(ResponseMessage.PRODUCT_DELETE_SUCCESS.getText()));
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(404).body(new MessageDTO(ResponseMessage.PRODUCT_DELETE_NOT_FOUND.getText()));
        } catch (Exception e){
            return ResponseEntity.status(500).body(new MessageDTO(ResponseMessage.PRODUCT_DELETE_FAIL.getText()));
        }
    }
}

