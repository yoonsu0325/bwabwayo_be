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
    private List<Double> vector;

    public static QdrantPointDto from(Long id, String title, List<Double> vector) {
        return QdrantPointDto.builder()
                .id(id)
                .title(title)
                .vector(vector)
                .build();
    }
}

