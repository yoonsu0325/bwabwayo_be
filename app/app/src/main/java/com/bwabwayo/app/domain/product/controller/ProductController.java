package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.dto.ResponseMessage;
import com.bwabwayo.app.domain.product.dto.request.ProductCreateAndUpdateRequestDTO;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.MessageDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductCreateResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductDetailResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductSearchResponseDTO;
import com.bwabwayo.app.domain.product.exception.UnauthorizedProductAccessException;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.user.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
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

    @Operation(summary = "상품 등록")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(schema = @Schema(implementation = ProductCreateResponseDTO.class))
            ),
            @ApiResponse(responseCode = "403"),
            @ApiResponse(responseCode = "500")
    })
    @PostMapping
    private ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductCreateAndUpdateRequestDTO requestDTO,
            @Parameter(hidden = true) @LoginUser User user
    ){
        // 로그인하지 않았다면 상품 등록 불가
        if(user == null){
            return ResponseEntity.status(403).body(ResponseMessage.PRODUCT_UNAUTHORIZATION.getText());
        }

        try{
            ProductCreateResponseDTO responseDTO = productService.createProduct(requestDTO, user);
            return ResponseEntity.ok(responseDTO);
        } catch(Exception e){
            return ResponseEntity.status(500).body(new MessageDTO(ResponseMessage.PRODUCT_SERVER_ERROR.getText()));
        }
    }

    @Operation(summary = "상품 목록 조회")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(schema = @Schema(implementation = ProductSearchResponseDTO.class))
            ),
            @ApiResponse(responseCode = "500")
    })
    @GetMapping
    public ResponseEntity<?> getProducts(@ModelAttribute ProductSearchRequestDTO requestDTO) {
        if(requestDTO.getPage() < 1) requestDTO.setPage(1);
        if(requestDTO.getSize() < 0) requestDTO.setSize(100);

        try{
            ProductSearchResponseDTO response = productService.searchProducts(requestDTO);
            return ResponseEntity.ok(response);
        } catch(Exception e){
            return ResponseEntity.status(500).body(new MessageDTO(ResponseMessage.PRODUCT_SERVER_ERROR.getText()));
        }
    }

    @Operation(summary = "상품 상세 조회")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(schema = @Schema(implementation = ProductDetailResponseDTO.class))),
            @ApiResponse(responseCode = "404"),
            @ApiResponse(responseCode = "500")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId){
        try{
            ProductDetailResponseDTO productDetail = productService.getProductDetail(productId);
            return ResponseEntity.ok(productDetail);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(404).body(new MessageDTO(ResponseMessage.PRODUCT_NOT_FOUND.getText()));
        } catch(Exception e){
            return ResponseEntity.status(500).body(new MessageDTO(ResponseMessage.PRODUCT_SERVER_ERROR.getText()));
        }
    }

    @Operation(summary = "상품 정보 갱신")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(schema = @Schema(implementation = ProductCreateResponseDTO.class))
            ),
            @ApiResponse(responseCode = "403"),
            @ApiResponse(responseCode = "404"),
            @ApiResponse(responseCode = "500")
    })
    @PutMapping("/{productId}")
    public ResponseEntity<MessageDTO> updateProduct(@PathVariable Long productId,
                                           @RequestBody ProductCreateAndUpdateRequestDTO requestDTO) {
        try {
            productService.updateProduct(productId, requestDTO);
            return ResponseEntity.ok(new MessageDTO(ResponseMessage.PRODUCT_UPDATE_SUCCESS.getText()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageDTO(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new MessageDTO(ResponseMessage.PRODUCT_SERVER_ERROR.getText()));
        }
    }

    @Operation(summary = "상품 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403"),
            @ApiResponse(responseCode = "404"),
            @ApiResponse(responseCode = "500")
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<MessageDTO> deleteProductById(
            @PathVariable Long productId,
            @Parameter(hidden = true) @LoginUser User user
    ){
        try{
            productService.deleteProductById(productId, user);
            return ResponseEntity.ok(new MessageDTO(ResponseMessage.PRODUCT_DELETE_SUCCESS.getText()));
        } catch (UnauthorizedProductAccessException e){
            log.error("ERROR 403: ", e);
            return ResponseEntity.status(403).body(new MessageDTO(ResponseMessage.PRODUCT_UNAUTHORIZATION.getText()));
        } catch (EntityNotFoundException e){
            log.error("ERROR 404: ", e);
            return ResponseEntity.status(404).body(new MessageDTO(ResponseMessage.PRODUCT_NOT_FOUND.getText()));
        } catch (Exception e){
            log.error("ERROR 500: ", e);
            return ResponseEntity.status(500).body(new MessageDTO(ResponseMessage.PRODUCT_SERVER_ERROR.getText()));
        }
    }
}

