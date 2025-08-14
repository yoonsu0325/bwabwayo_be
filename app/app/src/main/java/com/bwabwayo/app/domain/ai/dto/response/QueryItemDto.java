package com.bwabwayo.app.domain.ai.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QueryItemDto {
    private Long id;
    private String title;
    private String category;
    private Double score;
}
