package com.bwabwayo.app.domain.ai.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatBotResponse {

    private String answer;

    public ChatBotResponse(String answer) {
        this.answer = answer;
    }

    public static ChatBotResponse of(String answer) {
        return new ChatBotResponse(answer);
    }
}
