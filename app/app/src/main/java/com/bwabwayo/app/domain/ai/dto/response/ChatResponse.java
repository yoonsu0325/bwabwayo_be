package com.bwabwayo.app.domain.ai.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatResponse {

    private String answer;

    public ChatResponse(String answer) {
        this.answer = answer;
    }

    public static ChatResponse of(String answer) {
        return new ChatResponse(answer);
    }
}
