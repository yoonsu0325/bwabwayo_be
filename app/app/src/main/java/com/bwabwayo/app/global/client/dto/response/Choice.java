package com.bwabwayo.app.global.client.dto.response;

import com.bwabwayo.app.global.client.domain.OpenAiMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Choice {
    private OpenAiMessage message;
}
