package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.ai.domain.QdrantPointDto;
import com.bwabwayo.app.domain.ai.dto.response.SimilarResultResponse;
import com.bwabwayo.app.domain.ai.service.EmbeddingService;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.global.client.OpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSimilaritySearchService {

    private final EmbeddingService embeddingService;
    private final OpenAiClient openAiClient;

    /**
     * 상품 벡터 저장
     */
    public void upsert(Product product) {
        Long id = product.getId();
        String title = product.getTitle();
        String categoryName = product.getCategory().getName();

        Long categoryId = product.getCategory().getId();
        Integer price = product.getPrice();
        Boolean isSale = product.getSaleStatus() == SaleStatus.SOLD_OUT;

        // 임베딩 벡터 추출
        List<Double> titleVector = openAiClient.getEmbedding(title);
        List<Double> categoryVector = openAiClient.getEmbedding(categoryName);
        
        // Point DTO 생성
        QdrantPointDto qdrantPointDto = QdrantPointDto.builder()
                .id(id)
                .title(title)
                .categoryName(categoryName)
                .titleVector(titleVector)
                .categoryVector(categoryVector)
                .categoryId(categoryId)
                .price(price)
                .isSale(isSale)
                .build();

        embeddingService.upsertPoint(qdrantPointDto);
    }

    /**
     * 유사도 검색
     */
    public List<Long> query(String title, String category, int n) {
        // 벡터화
        List<Double> titleQueryVector = openAiClient.getEmbedding(title);
        List<Double> categoryQueryVector = openAiClient.getEmbedding(category);

        // 유사도 검색 (Top N)
        return embeddingService.query(titleQueryVector, categoryQueryVector,  n)
                .stream().map(SimilarResultResponse::getId).toList();
    }

    /**
     * 상품 벡터 삭제
     */
    public void deleteById(Long productId) {
        embeddingService.deleteById(productId);
    }
}
