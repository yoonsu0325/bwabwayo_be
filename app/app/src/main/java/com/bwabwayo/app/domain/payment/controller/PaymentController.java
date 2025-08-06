package com.bwabwayo.app.domain.payment.controller;

import com.bwabwayo.app.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    @Value("${toss.url.confirm}")
    private String TOSS_CONFIRM_URL;
    @Value("${toss.key.client-key}")
    private String TOSS_CLIENT_KEY;
    @Value("${toss.key.secret-key}")
    private String TOSS_SECRET_KEY;


    private final RestTemplate restTemplate;
    RedisTemplate<String, Integer> paymentRedisTemplate;

    /**
     * 결제 금액 임시 저장
     */
    @PostMapping("/saveAmount")
    public ResponseEntity<?> tempSave(@RequestParam String orderId, @RequestParam Integer amount) {
        // Redis에 결제 금액 저장 (예: 유효 시간 10분 설정)
        String key = "payment:amount:" + orderId;
        paymentRedisTemplate.opsForValue().set(key, amount, Duration.ofMinutes(10));

        return ResponseEntity.ok().build();
    }


    /**
     * 결제 금액을 검증
     */
    @PostMapping("/verifyAmount")
    public ResponseEntity<?> verifyAmount(@RequestParam String orderId, @RequestParam Integer amount) {
        String key = "payment:amount:" + orderId;
        Integer storedAmount = paymentRedisTemplate.opsForValue().get(key);
        paymentRedisTemplate.delete(key);

        if (storedAmount == null) {
            throw new BadRequestException("결제 정보가 존재하지 않습니다.");
        }
        if (!storedAmount.equals(amount)) {
            throw new BadRequestException("결제 금액이 일치하지 않습니다.");
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 토스에 결제 승인받기
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestParam String orderId,
            @RequestParam Integer amount, @RequestParam String paymentKey
    ) {
        // 1. Authorization 헤더 생성
        String encodedAuth = Base64.getEncoder().encodeToString((TOSS_SECRET_KEY + ":").getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);

        // 2. Request Body 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount);

        // 3. HttpEntity 생성
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 4. POST 요청
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(TOSS_CONFIRM_URL, entity, String.class);
            log.info("응답: {}", response.getBody());
            System.out.println();
        } catch (Exception e) {
            log.info("결제 승인 실패: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
