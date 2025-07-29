package com.bwabwayo.app.domain.ai.controller;

import com.bwabwayo.app.domain.ai.dto.request.ChatRequest;
import com.bwabwayo.app.domain.ai.dto.response.OpenAiRecommendationResponse;
import com.bwabwayo.app.domain.ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class ChatController {

    private final ChatService chatService;

    // 챗봇 응답 전체
    @PostMapping
    public ResponseEntity<OpenAiRecommendationResponse> chat(@RequestBody ChatRequest chatRequest) {
        OpenAiRecommendationResponse response = chatService.getRecommendation(chatRequest.getMessage());
        return ResponseEntity.ok(response);
    }

    // 추천한 상품 유사도 검색
}
