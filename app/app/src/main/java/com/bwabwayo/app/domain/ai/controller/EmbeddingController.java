package com.bwabwayo.app.domain.ai.controller;

import com.bwabwayo.app.domain.ai.dto.response.QueryItemDto;
import com.bwabwayo.app.domain.ai.service.EmbeddingService;
import com.bwabwayo.app.domain.ai.service.ProductEmbeddingService;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.dto.ProductQueryCondition;
import com.bwabwayo.app.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/embedding")
public class EmbeddingController {
    private final EmbeddingService embeddingService;
    private final ProductService productService;
    private final ProductEmbeddingService productEmbeddingService;


    @Operation(summary = "[TEST] 상품을 벡터 DB에 저장")
    @PostMapping("/save/{productId}")
    public ResponseEntity<?> upsert(@PathVariable Long productId) {
        Product product = productService.findById(productId);
        productEmbeddingService.upsert(product);
        return ResponseEntity.ok(Map.of("result", "Qdrant에 벡터 저장 완료"));
    }
    
    @Operation(summary = "[TEST] 유사도 검색 (전체 검색)")
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "3") Integer limit
    ) {
        List<QueryItemDto> queryResult = productEmbeddingService.query(
                ProductQueryCondition.builder().keyword(keyword).build(),
                PageRequest.of(0, limit)
        );
        return ResponseEntity.ok(Map.of("results", queryResult));
    }

    @Operation(summary = "[TEST] 상품을 벡터 DB에서 삭제")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deletePoint(@RequestBody Long productId) {
        productEmbeddingService.deleteById(productId);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "[TEST] Qdrant point 목록 조회")
    @GetMapping("/scroll")
    public ResponseEntity<?> scroll(@RequestParam Integer limit){
        return ResponseEntity.ok(embeddingService.getPoints(limit));
    }
}
