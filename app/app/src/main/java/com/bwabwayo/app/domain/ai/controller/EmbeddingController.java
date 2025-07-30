package com.bwabwayo.app.domain.ai.controller;

import com.bwabwayo.app.domain.ai.domain.QdrantPointDto;
import com.bwabwayo.app.domain.ai.dto.request.PonintRequest;
import com.bwabwayo.app.domain.ai.dto.response.SimilarResultResponse;
import com.bwabwayo.app.domain.ai.service.EmbeddingService;
import com.bwabwayo.app.global.client.OpenAiClient;
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


    // 판매게시글 임베팅 벡터화 후 Qdrant에 벡터 저장
    @PostMapping("/save")
    public ResponseEntity<String> savePoint(@RequestBody PonintRequest ponintRequest) {

        // 1. title 추출
        Long id = ponintRequest.getId();
        String title = ponintRequest.getTitle();

        // 2. 임베딩 벡터 추출
        List<Double> vector = openAiClient.getEmbedding(title);

        // 3. QdrantPointDto 생성
        QdrantPointDto qdrantPointDto = QdrantPointDto.from(id, title, vector);

        embeddingService.saveToQdrant(qdrantPointDto);
        return ResponseEntity.ok("Qdrant에 벡터 저장 완료");
    }

    // 유사도 검색
    @PostMapping("/search")
    public ResponseEntity<List<SimilarResultResponse>> searchSimilarTitles(@RequestBody PonintRequest request) {
        String inputTitle = request.getTitle();

        // 벡터화
        List<Double> queryVector = openAiClient.getEmbedding(inputTitle);

        // 유사도 검색 (Top 3)
        List<SimilarResultResponse> similarTitles = embeddingService.searchSimilarTitles(queryVector, 3);

        return ResponseEntity.ok(similarTitles);
    }


    // Qdrant에 저장되어 있는 벡터 데이터 삭제
    @PostMapping("/delete")
    public ResponseEntity<?> deletePoint(@RequestBody Map<String, List<Long>> requestBody) {
        Long id = requestBody.get("points").get(0);
        embeddingService.deleteFromQdrantById(id);
        return ResponseEntity.ok().build();
    }
}
