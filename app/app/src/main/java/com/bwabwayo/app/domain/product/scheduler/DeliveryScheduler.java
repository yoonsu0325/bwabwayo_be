package com.bwabwayo.app.domain.product.scheduler;

import com.bwabwayo.app.domain.product.domain.Courier;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.dto.response.TrackingDetail;
import com.bwabwayo.app.domain.product.dto.response.TrackingInfoResponse;
import com.bwabwayo.app.domain.product.enums.DeliveryStatus;
import com.bwabwayo.app.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryScheduler {
    private final ProductService productService;
    private final RestTemplate restTemplate;

    @Value("${sweet-tracker.api-key}")
    private String apiKey;

    // 1일에 1번 (매일 자정)
    @Scheduled(cron = "0 0 0 * * *")
    public void runDaily() { updateDeliveryStatus(DeliveryStatus.PREPARING);}

    // 12시간마다 (자정과 정오)
    @Scheduled(cron = "0 0 0,12 * * *")
    public void runEvery12Hours() {
        updateDeliveryStatus(DeliveryStatus.COLLECTED);
    }

    // 6시간마다 (자정, 6시, 12시, 18시)
    @Scheduled(cron = "0 0 0,6,12,18 * * *")
    public void runEvery6Hours() {
        updateDeliveryStatus(DeliveryStatus.IN_TRANSIT);
    }

    // 3시간마다 (자정, 3시, 6시, 9시, 12시, 15시, 18시, 21시)
    @Scheduled(cron = "0 0 0,3,6,9,12,15,18,21 * * *")
    public void runEvery3Hours() {
        updateDeliveryStatus(DeliveryStatus.ARRIVED_AT_BRANCH);
    }

    // 1시간마다 (매시 0분 0초)
    @Scheduled(cron = "0 0 * * * *")
    public void runEveryHour() {
        updateDeliveryStatus(DeliveryStatus.OUT_FOR_DELIVERY);
    }

    private
    void updateDeliveryStatus(DeliveryStatus deliveryStatus){
        log.info("'{}' 상태 상품 갱신", deliveryStatus.getDescription());
        List<Product> products = productService.getDeliveringProductsByDeliveryStatus(deliveryStatus);

        for (Product product : products){
            Courier courier = product.getCourier();
            DeliveryStatus maxStatus = DeliveryStatus.fromLevel(getMaxLevel(courier.getCode(), product.getInvoiceNumber()));
            product.setDeliveryStatus(maxStatus);
        }
    }

    private int getMaxLevel(String tCode, String tInvoice) {
        String url = UriComponentsBuilder.fromUriString("https://info.sweettracker.co.kr/api/v1/trackingInfo")
                .queryParam("t_key", apiKey)
                .queryParam("t_code", tCode)
                .queryParam("t_invoice", tInvoice)
                .toUriString();

        ResponseEntity<TrackingInfoResponse> response = restTemplate.getForEntity(url, TrackingInfoResponse.class);
        TrackingInfoResponse body = response.getBody();

        if (body != null && body.getTrackingDetails() != null) {
            return body.getTrackingDetails().stream()
                    .mapToInt(TrackingDetail::getLevel)
                    .max()
                    .orElse(0);
        }

        return 0;
    }
}
