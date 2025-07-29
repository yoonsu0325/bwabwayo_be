package com.bwabwayo.app.domain.ai.controller;

import com.bwabwayo.app.domain.ai.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/embedding")
public class EmbeddingController {

    private final EmbeddingService embeddingService;


    //게시글 제목을 임베딩 벡터로 변환
    @PostMapping
    public List<Double> getEmbedding(@RequestBody String title) {
        return embeddingService.embedTitle(title);
    }

}
