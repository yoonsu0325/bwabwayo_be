package com.bwabwayo.app.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GptConfig {

    @Value("${openai.secret-key}")
    private String secretKey;


    /**
     * RestTemplate Bean 등록
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .additionalInterceptors(((request, body, execution) -> {
                    // step 1. Authorization 헤더 추가 (Bearer + API KEY)
                    request.getHeaders().add("Authorization", "Bearer " + secretKey);
                    // step 2. Content-Type 설정 (application/json)
                    request.getHeaders().add("Content-Type", "application/json");
                    // step 3. 요청 실행
                    return execution.execute(request, body);
                }))
                .build();
    }
}
