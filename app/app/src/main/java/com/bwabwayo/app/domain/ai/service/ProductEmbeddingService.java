package com.bwabwayo.app.domain.ai.service;

import com.bwabwayo.app.domain.ai.domain.QdrantPointDto;
import com.bwabwayo.app.domain.ai.dto.response.QueryItemDto;
import com.bwabwayo.app.domain.ai.util.QdrantFilterBuilder;
import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.dto.ProductQueryCondition;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.domain.product.service.CategoryService;
import com.bwabwayo.app.global.client.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEmbeddingService {

    private final OpenAiClient openAiClient;
    private final EmbeddingService embeddingService;
    private final CategoryService categoryService;
    private final SecondhandTextPreprocessingService textPreprocessingService;

    /**
     * 상품 벡터 저장
     */
    public void upsert(Product product) {
        // 속성 추출
        Long id = product.getId();
        String title = product.getTitle();
        String categoryName = product.getCategory().getName();

        Long categoryId = product.getCategory().getId();
        Integer price = product.getPrice();
        Boolean isSale = product.getSaleStatus() == SaleStatus.SOLD_OUT;

        // 텍스트 전처리
        String preprocessedTitle = textPreprocessingService.preprocessForSecondhand(title);
        if(preprocessedTitle == null || preprocessedTitle.isEmpty()) {
            preprocessedTitle = title;
        }

        // 임베딩 벡터 추출
        List<Double> titleVector = openAiClient.getEmbedding(preprocessedTitle);
        List<Double> categoryVector = openAiClient.getEmbedding(categoryName);

        // Point DTO 생성
        QdrantPointDto qdrantPointDto = QdrantPointDto.of(
                id, title, titleVector,
                categoryName, categoryVector, categoryId,
                price, isSale,
                preprocessedTitle
        );
        embeddingService.upsertPoint(qdrantPointDto);
    }

    /**
     * 상품 벡터 삭제
     */
    public void deleteById(Long productId) {
        embeddingService.deleteById(productId);
    }

    /**
     * 상품 검색
     */
    public List<QueryItemDto> query(ProductQueryCondition queryCondition, Pageable pageable) {
        // 벡터 대상 추출
        String title = queryCondition.getKeyword(); // 상품명
        Long categoryId =  queryCondition.getCategoryId(); // 카테고리
        Category category = categoryId != null ? categoryService.findById(categoryId) : null;

        // 전처리
        String preprocessedTitle = textPreprocessingService.preprocessForSecondhand(title);
        if(preprocessedTitle == null || preprocessedTitle.isEmpty()) {
            preprocessedTitle = title;
        }

        // 벡터화
        List<Double> titleQueryVector = openAiClient.getEmbedding(preprocessedTitle);
        List<Double> categoryQueryVector = category != null ? openAiClient.getEmbedding(category.getName()) : titleQueryVector;
        
        // 필터 생성
        List<Long> categoryIn = queryCondition.getCategoryIn();
        Integer minPrice = queryCondition.getMinPrice();
        Integer maxPrice = queryCondition.getMaxPrice();
        Boolean onlySale = queryCondition.getGetOnlySale();

        QdrantFilterBuilder builder = new QdrantFilterBuilder();
        if(categoryIn != null && !categoryIn.isEmpty()) builder.in("categoryId", categoryIn);
        builder.range("price", minPrice, maxPrice);
        if(onlySale != null && onlySale) builder.match("isSale", false);
        Map<String, Object> filter = builder.build();

        // 유사도 검색
        return embeddingService.query(titleQueryVector, categoryQueryVector, pageable, filter, true);
    }
}
