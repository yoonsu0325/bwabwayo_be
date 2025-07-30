package com.bwabwayo.app.global.client.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbeddingResponse {
    private List<EmbeddingData> data;
}