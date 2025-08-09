package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.ai.domain.QdrantPointDto;
import com.bwabwayo.app.domain.ai.dto.response.SimilarResultResponse;
import com.bwabwayo.app.domain.ai.service.EmbeddingService;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.global.client.OpenAiClient;
import lombok.RequiredArgsConstructor;
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
        String category = product.getCategory().getName();

        // 2. 임베딩 벡터 추출
        List<Double> titleVector = openAiClient.getEmbedding(title);
        List<Double> categoryVector = openAiClient.getEmbedding(category);

        // 3. QdrantPointDto 생성
        QdrantPointDto qdrantPointDto = QdrantPointDto.from(id, title, category, titleVector, categoryVector);

        embeddingService.upsertPoint(qdrantPointDto);
    }

    /**
     * 유사도 검색
     */
    public List<Long> searchSimilarTitles(String title, String category, int n) {
        // 벡터화
        List<Double> titleQueryVector = openAiClient.getEmbedding(title);
        List<Double> categoryQueryVector = openAiClient.getEmbedding(category);

        // 유사도 검색 (Top N)
        return embeddingService.query(titleQueryVector, categoryQueryVector,  n)
                .stream().map(SimilarResultResponse::getId).toList();
    }

    /**
     * 벡터 삭제
     */
    public void deleteById(Long productId) {
        embeddingService.deleteById(productId);
    }
}
