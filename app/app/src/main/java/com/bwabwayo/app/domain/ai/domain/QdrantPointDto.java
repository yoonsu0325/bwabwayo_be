package com.bwabwayo.app.domain.ai.domain;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QdrantPointDto {
    private Long id;

    private String title;
    private String categoryName;

    private Long categoryId;
    private Integer price;
    private Boolean isSale;

    private List<Double> titleVector;
    private List<Double> categoryVector;


    public static QdrantPointDto of(Long id, String title, List<Double> titleVector){
        return QdrantPointDto.of(id, title, titleVector, title, titleVector, null, null, null);
    }

    public static QdrantPointDto of(
            Long id,
            String title, List<Double> titleVector,
            String category, List<Double> categoryVector
    ){
        return QdrantPointDto.of(id, title, titleVector, category, categoryVector, null, null, null);
    }

    public static QdrantPointDto of(
            Long id,
            String title, List<Double> titleVector,
            String category, List<Double> categoryVector,
            Long categoryId, Integer price, Boolean isSale
    ){
        return QdrantPointDto.builder()
                .id(id)
                .title(title).titleVector(titleVector)
                .categoryName(category).categoryVector(categoryVector)
                .categoryId(categoryId).price(price).isSale(isSale)
                .build();
    }
}

