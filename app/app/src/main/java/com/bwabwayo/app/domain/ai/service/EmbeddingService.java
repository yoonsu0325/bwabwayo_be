package com.bwabwayo.app.domain.ai.service;

import com.bwabwayo.app.global.client.OpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final OpenAiClient openAiClient;

    // 게시글 제목을 임베딩 벡터로 변환
    public List<Double> embedTitle(String title) {
        return openAiClient.getEmbedding(title);
    }
}
