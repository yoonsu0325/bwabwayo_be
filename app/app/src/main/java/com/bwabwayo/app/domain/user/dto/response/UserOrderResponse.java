package com.bwabwayo.app.domain.user.dto.response;

import com.bwabwayo.app.domain.product.enums.SaleStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserOrderResponse {
    private Long saleId;
    private Long productId;
    private String thumbnail;
    private String title;
    private LocalDateTime createdAt;
    private int price;
    private String deliveryStatus;
    private String courierName;
    private String trackingNumber;
    int PurchaseConfirmStatus;
}

