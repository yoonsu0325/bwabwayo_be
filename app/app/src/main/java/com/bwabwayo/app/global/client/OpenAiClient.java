package com.bwabwayo.app.global.client;


import com.bwabwayo.app.global.client.domain.OpenAiMessage;
import com.bwabwayo.app.global.client.dto.request.EmbeddingRequest;
import com.bwabwayo.app.global.client.dto.request.OpenAiRequest;
import com.bwabwayo.app.global.client.dto.response.EmbeddingResponse;
import com.bwabwayo.app.global.client.dto.response.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final RestTemplate restTemplate;

    @Value("${openai.chat-url}")
    private String apiUrl;
    @Value("${openai.chat-model}")
    private String model;

    /**
     * 사용자 질문을 GPT 모델에 전달하고 응답 받기
     */
    public OpenAiResponse getChatCompletion(String prompt) {
        // step 1. OpenAI 요청 구성
        OpenAiRequest openAiRequest = getOpenAiRequest(prompt);

        // step 2. RestTemplate을 통해 OpenAI API POST 요청 전송
        ResponseEntity<OpenAiResponse> chatResponse = restTemplate.postForEntity(
                apiUrl,
                openAiRequest,
                OpenAiResponse.class
        );

        // step 3. 응답 실패 처리
        if (!chatResponse.getStatusCode().is2xxSuccessful() || chatResponse.getBody() == null) {
            throw new RuntimeException("OpenAI API 호출 실패");
        }

        // step 4. 성공 시 응답 본문 반환
        return chatResponse.getBody();
    }

    /**
     * OpenAI 요청 구성
     */
    private OpenAiRequest getOpenAiRequest(String prompt) {
        // step 1-1. system 메세지 작성 - AI 역할 지시
        OpenAiMessage systemMessage = new OpenAiMessage(
                "system",
                "너는 중고거래 플랫폼의 도우미야. 사용자 질문에 대해 중고 제품을 2개 ~ 3개 정도 추천해줘 제품 가격은 범위를 좁게 한국 원화로해줘. 응답은 반드시 아래 형식의 **JSON만** 보내. JSON 이외의 문장은 금지야.\n\n" +
                        "{\n" +
                        "  \"products\": [\n" +
                        "    {\n" +
                        "      \"name\": \"제품명\",\n" +
                        "      \"feature\": \"특징\",\n" +
                        "      \"priceRange\": \"가격대\",\n" +
                        "      \"advantage\": \"장점\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
        );
        // step 1-2. user 메세지 작성 - 실제 사용자의 질문
        OpenAiMessage userMessage = new OpenAiMessage("user", prompt);
        // step 1-3. 메세지 리스트에 system → user 순서로 담기
        List<OpenAiMessage> messages = List.of(systemMessage, userMessage);
        // step 1-4. 모델 이름과 메세지를 포함한 요청 객체 생성
        return new OpenAiRequest(model, messages, 0.1);
    }


    public List<Double> getEmbedding(String text) {
        EmbeddingRequest request = new EmbeddingRequest("text-embedding-3-large", List.of(text));

        ResponseEntity<EmbeddingResponse> response = restTemplate.postForEntity(
                "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings",  // 또는 공식 OpenAI URL
                request,
                EmbeddingResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("임베딩 API 호출 실패");
        }

        return response.getBody().getData().get(0).getEmbedding();
    }



}