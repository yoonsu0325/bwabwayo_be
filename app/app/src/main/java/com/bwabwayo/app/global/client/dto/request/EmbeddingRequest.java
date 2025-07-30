package com.bwabwayo.app.global.client.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EmbeddingRequest {
    private String model;
    private List<String> input;
}
