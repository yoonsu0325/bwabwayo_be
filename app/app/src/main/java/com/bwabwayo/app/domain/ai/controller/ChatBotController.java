package com.bwabwayo.app.domain.ai.controller;

import com.bwabwayo.app.domain.ai.dto.request.ChatBotRequest;
import com.bwabwayo.app.domain.ai.dto.response.OpenAiRecommendationResponse;
import com.bwabwayo.app.domain.ai.dto.response.RecommendAiProductResponse;
import com.bwabwayo.app.domain.ai.service.ChatBotService;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class ChatBotController {

    private final ChatBotService chatBotService;
    private final ProductService productService;
    private final StorageService storageService;

    // 챗봇 응답 전체
    @PostMapping
    public ResponseEntity<OpenAiRecommendationResponse> chat(@RequestBody ChatBotRequest chatBotRequest) {
        OpenAiRecommendationResponse response = chatBotService.getRecommendation(chatBotRequest.getMessage());
        return ResponseEntity.ok(response);
    }

    // 추천한 상품에 관련된 판매글
    @GetMapping
    public ResponseEntity<List<RecommendAiProductResponse>> getRecommendation(@RequestParam String keyword) {
        List<Product> products = productService.recommendTopK(keyword, 3);

        List<RecommendAiProductResponse> reList = products.stream().map(p ->
                RecommendAiProductResponse.builder()
                        .id(p.getId())
                        .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                        .categoryName(p.getCategory() != null ? p.getCategory().getName() : null) // 필드명에 맞게
                        .title(p.getTitle())
                        .price(p.getPrice())
                        .imageUrls(
                                p.getProductImages() == null ? List.of() :
                                        p.getProductImages().stream()
                                                .map(img -> storageService.getUrlFromKey(p.getThumbnail()))
                                                .toList()
                        )
                        .build()
        ).toList();

        return ResponseEntity.ok(reList);
    }
}
