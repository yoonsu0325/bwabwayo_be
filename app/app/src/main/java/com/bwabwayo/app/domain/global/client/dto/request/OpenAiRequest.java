package com.bwabwayo.app.domain.global.client.dto.request;

import com.bwabwayo.app.domain.global.client.domain.OpenAiMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenAiRequest {

    private String model;
    private List<OpenAiMessage> messages;
    private double temperature;

    public OpenAiRequest(String model, List<OpenAiMessage> messages) {
        this(model, messages, 0.1); // 기본값 0.3
    }

    public OpenAiRequest(String model, List<OpenAiMessage> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }
}
