package com.bwabwayo.app.domain.global.client.dto.response;

import com.bwabwayo.app.domain.global.client.domain.OpenAiMessage;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenAiResponse {

    // GPT가 생성한 응답 선택지 리스트
    private List<Choice> choices;

    @Getter
    public static class Choice {
        // 생성된 메세지 정보
        private OpenAiMessage message;
    }
}