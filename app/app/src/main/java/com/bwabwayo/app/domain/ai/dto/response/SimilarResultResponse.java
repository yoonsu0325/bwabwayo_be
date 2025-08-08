package com.bwabwayo.app.domain.ai.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SimilarResultResponse {

    private long id;
    private String title;
    private String category;
    private double score;
}
