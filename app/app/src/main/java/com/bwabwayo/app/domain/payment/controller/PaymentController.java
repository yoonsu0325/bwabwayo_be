package com.bwabwayo.app.domain.payment.controller;

import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.payment.dto.request.PaymentConfirmRequest;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.domain.Sale;
import com.bwabwayo.app.domain.product.enums.PaymentStatus;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.product.service.SaleService;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.service.UserService;
import com.bwabwayo.app.global.exception.BadRequestException;
import com.bwabwayo.app.global.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final SaleService saleService;
    private final ProductService productService;
    private final UserService userService;
    @Value("${toss.url.confirm}")
    private String TOSS_CONFIRM_URL;
    @Value("${toss.key.secret-key}")
    private String TOSS_SECRET_KEY;


    RedisTemplate<String, Integer> paymentRedisTemplate;
    private final ObjectMapper objectMapper;


    /**
     * 결제 금액 임시 저장
     */
    @PostMapping("/saveAmount")
    public ResponseEntity<?> tempSave(@RequestParam String orderId, @RequestParam Integer amount) {
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
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentConfirmRequest requestDTO, @LoginUser User loginUser) throws IOException {
        log.info("confirmPayment를 호출: {}, loginUserId={}", requestDTO, loginUser.getId());
        log.info("loginUser.ID={}", loginUser.getId());
        String buyerId = loginUser.getId();
        Long productId = requestDTO.getProductId();

//        User buyer = userService.findById(buyerId);
        Product product = productService.findById(productId);

        Sale sale = null;
        try {
//            sale = saleService.findByProductId(productId);
            sale = saleService.findByBuyerIdAndProductId(buyerId, productId);
            if(sale.getPaymentStatus() == PaymentStatus.COMPLETED){
                log.warn("이미 완료된 요청입니다: saleId={}", sale.getId());
//                throw new BadRequestException("중복 결제 요청입니다.");
            }
        } catch (IllegalArgumentException e) {
            log.warn("사전에 등록되지 않은 거래에 대한 결제 요청 입니다: productId={}, sellerId={}, buyerId={}, amount={}, error={}",
                    product.getId(),
                    product.getSeller().getId(),
                    buyerId,
                    requestDTO.getAmount(), e.getMessage()
            );

            sale = Sale.builder()
                    .product(product)
                    .buyerId(buyerId)
                    .sellerId(product.getSeller().getId())
                    .salePrice(requestDTO.getAmount())
                    .build();

            saleService.saveSale(sale);
//            throw new NotFoundException(e.getMessage());
        }

        String jsonBody = serialize(requestDTO);
        JSONObject response = sendRequest(parseRequestData(jsonBody), TOSS_SECRET_KEY, TOSS_CONFIRM_URL);
        int statusCode = response.containsKey("error") ? 400 : 200;
        if (statusCode == 200) {
            log.info("결제 성공: {}", requestDTO);
            saleService.changePaymentStatus(sale.getId(), PaymentStatus.COMPLETED);
        } else {
            log.info("결제 실패: {}", requestDTO);
            saleService.changePaymentStatus(sale.getId(), PaymentStatus.FAILED);
        }

        return ResponseEntity.status(statusCode).body(response);
    }

    private JSONObject sendRequest(JSONObject requestData, String secretKey, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(secretKey, urlString);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream responseStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            log.error("Error reading response", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Error reading response");
            return errorResponse;
        }
    }

    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }

    private JSONObject parseRequestData(String jsonBody) {
        try {
            return (JSONObject) new JSONParser().parse(jsonBody);
        } catch (ParseException e) {
            log.error("JSON Parsing Error", e);
            return new JSONObject();
        }
    }

    private String serialize(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("직렬화 실패: " + e.getMessage(), e);
        }
    }
}
