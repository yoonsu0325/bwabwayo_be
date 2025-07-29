package com.bwabwayo.app.domain.global.client.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class EmbeddingResponse {
    private List<EmbeddingData> data;

    @Getter
    public static class EmbeddingData {
        private List<Double> embedding;
        private int index;
    }
}