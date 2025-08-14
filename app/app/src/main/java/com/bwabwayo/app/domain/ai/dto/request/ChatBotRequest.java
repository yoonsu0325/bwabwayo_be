package com.bwabwayo.app.domain.ai.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatBotRequest {

    private String message;

    public ChatBotRequest(String message) {
        this.message = message;
    }
}
