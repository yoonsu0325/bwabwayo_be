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

    public static QdrantPointDto from(Long id, String title, String category,
                                      List<Double> titleVec, List<Double> categoryVec) {
        return QdrantPointDto.builder()
                .id(id).title(title).categoryName(category)
                .titleVector(titleVec).categoryVector(categoryVec)
                .build();
    }
}

