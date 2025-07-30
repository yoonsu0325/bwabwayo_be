package com.bwabwayo.app.global.client.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)  // ★ 이 줄 추가
public class OpenAiResponse {
    private List<Choice> choices;

}