package com.bwabwayo.app.domain.global.client.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenAiMessage {

    // 메세지 역할 (user, assistant, system)
    private String role;

    // 메세지 내용
    private String content;

    public OpenAiMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
