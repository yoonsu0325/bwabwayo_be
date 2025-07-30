package com.bwabwayo.app.domain.ai.service;

import com.bwabwayo.app.domain.ai.dto.response.OpenAiRecommendationResponse;
import com.bwabwayo.app.global.client.OpenAiClient;
import com.bwabwayo.app.global.client.dto.response.OpenAiResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    /**
     * GPT 응답에서 전체 제품 정보 객체 반환
     */
    public OpenAiRecommendationResponse getRecommendation(String question) {
        OpenAiResponse openAiResponse = openAiClient.getChatCompletion(question);
        String responseBody = openAiResponse.getChoices().get(0).getMessage().getContent();

        try {
            return objectMapper.readValue(responseBody, OpenAiRecommendationResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("GPT 응답 파싱 실패: " + responseBody, e);
        }
    }

    /*
     * 제품명만 추출
     */
//    public List<String> getRecommendedProductNames(String question) {
//        return getRecommendation(question)
//                .getProducts().stream()
//                .map(OpenAiRecommendationResponse.ProductInfo::getName)
//                .collect(Collectors.toList());
//    }

    /*
     * GPT 응답 원문 반환
     */
//    public String getAnswer(String question) {
//        OpenAiResponse openAiResponse = openAiClient.getChatCompletion(question);
//        return openAiResponse.getChoices().get(0).getMessage().getContent();
//    }
}
