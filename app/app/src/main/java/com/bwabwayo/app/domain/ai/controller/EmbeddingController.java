package com.bwabwayo.app.domain.ai.controller;

import com.bwabwayo.app.domain.ai.domain.QdrantPointDto;
import com.bwabwayo.app.domain.ai.dto.response.QueryItemDto;
import com.bwabwayo.app.domain.ai.service.EmbeddingService;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.global.client.OpenAiClient;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/embedding")
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    private final OpenAiClient openAiClient;
    private final ProductService productService;

    @Operation(summary = "[TEST] 상품을 벡터 DB에 저장")
    @PostMapping("/save/{productId}")
    public ResponseEntity<String> savePoint(@PathVariable Long productId) {
        Product product = productService.findById(productId);

        // 1. title 추출
        Long id = product.getId();
        String title = product.getTitle();
        String category = product.getCategory().getName();

        // 2. 임베딩 벡터 추출
        List<Double> titleVec = openAiClient.getEmbedding(title);
        List<Double> categoryVec = openAiClient.getEmbedding(category);

        // 3. QdrantPointDto 생성
        QdrantPointDto qdrantPointDto = QdrantPointDto.from(id, title, category, titleVec, categoryVec);

        embeddingService.upsertPoint(qdrantPointDto);
        return ResponseEntity.ok("Qdrant에 벡터 저장 완료");
    }
    
    @Operation(summary = "[TEST] 유사도 검색 (전체 검색)")
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "3") Integer limit
    ) {
        List<Double> titleVector = openAiClient.getEmbedding(keyword);
        List<Double> categoryVector = titleVector;

        List<QueryItemDto> queryResult = embeddingService.query(titleVector, categoryVector, limit);

        return ResponseEntity.ok(Map.of("results", queryResult));
    }

    @Operation(summary = "[TEST] 상품을 벡터 DB에서 삭제")
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<Void> deletePoint(@PathVariable Long productId) {
        embeddingService.deleteById(productId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[TEST] 저장된 Point의 목록 반환")
    @GetMapping("/scroll")
    public ResponseEntity<?> scroll(@RequestParam(defaultValue = "100") Integer limit){
        return embeddingService.getPoints(limit);
    }
}
