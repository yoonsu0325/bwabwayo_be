package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.ai.dto.response.QueryItemDto;
import com.bwabwayo.app.domain.ai.service.ProductEmbeddingService;
import com.bwabwayo.app.domain.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSimilaritySearchService {

    private final ProductEmbeddingService productEmbeddingService;

    /**
     * 상품 벡터 저장
     */
    public void upsert(Product product) {
        productEmbeddingService.upsert(product);
    }

    /**
     * 유사도 검색
     */
    public List<Long> query(String title, String category, int n) {
        return productEmbeddingService.query(title, category, n)
                .stream().map(QueryItemDto::getId).toList();
    }

    /**
     * 상품 벡터 삭제
     */
    public void deleteById(Long productId) {
        productEmbeddingService.deleteById(productId);
    }
}
