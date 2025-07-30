package com.bwabwayo.app.global.client.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
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
