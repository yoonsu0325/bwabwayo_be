package com.bwabwayo.app.domain.ai.controller;

import com.bwabwayo.app.domain.ai.dto.request.ChatBotRequest;
import com.bwabwayo.app.domain.ai.dto.response.OpenAiRecommendationResponse;
import com.bwabwayo.app.domain.ai.dto.response.RecommendAiProductResponse;
import com.bwabwayo.app.domain.ai.service.ChatBotService;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.service.ProductService;
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

    // 챗봇 응답 전체
    @PostMapping
    public ResponseEntity<OpenAiRecommendationResponse> chat(@RequestBody ChatBotRequest chatBotRequest) {
        OpenAiRecommendationResponse response = chatBotService.getRecommendation(chatBotRequest.getMessage());
        return ResponseEntity.ok(response);
    }

    // 추천한 상품에 관련댄 판매글
    @GetMapping
    public ResponseEntity<List<RecommendAiProductResponse>> getRecommendation(@RequestParam String keyword){
        List<Product> products = productService.recommendTopK(keyword,3);
        List<RecommendAiProductResponse> reList = products.stream().map(Product->
                RecommendAiProductResponse.builder()
                        .id(Product.getId())
                        .title(Product.getTitle())
                        .category(Product.getCategory())
                        .price(Product.getPrice())
                        .productImages(Product.getProductImages())
                        .build()).collect(Collectors.toList());
        return ResponseEntity.ok(reList);
    }
}
