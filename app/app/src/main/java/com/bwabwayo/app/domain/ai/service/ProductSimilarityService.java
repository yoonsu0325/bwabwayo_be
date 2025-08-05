package com.bwabwayo.app.domain.ai.service;

import com.bwabwayo.app.domain.ai.domain.QdrantPointDto;
import com.bwabwayo.app.domain.ai.dto.response.SimilarResultResponse;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.global.client.OpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSimilarityService {

    private final EmbeddingService embeddingService;
    private final OpenAiClient openAiClient;

    /**
     * 판매글 벡터 저장
     */
    public void savePoint(Product product) {
        // 1. title 추출
        Long id = product.getId();
        String title = product.getTitle();

        // 2. 임베딩 벡터 추출
        List<Double> vector = openAiClient.getEmbedding(title);

        // 3. QdrantPointDto 생성
        QdrantPointDto qdrantPointDto = QdrantPointDto.from(id, title, vector);

        embeddingService.saveToQdrant(qdrantPointDto);
    }

    /**
     * 유사도 검색
     */
    public List<SimilarResultResponse> searchSimilarTitles(String title, int n) {
        // 벡터화
        List<Double> queryVector = openAiClient.getEmbedding(title);

        // 유사도 검색 (Top N)
        return embeddingService.searchSimilarTitles(queryVector, n);
    }

    /**
     * 벡터 삭제
     */
    public void deletePoint(Long productId) {
        embeddingService.deleteFromQdrantById(productId);
    }
}
