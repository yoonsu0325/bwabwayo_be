package com.bwabwayo.app.domain.user.dto.response;

import com.bwabwayo.app.domain.product.enums.SaleStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserOrderResponse {
    private Long saleId;
    private Long productId;
    private String thumbnail;
    private String title;
    private int price;
    private String deliveryStatus;
    private String courierName;
    private String trackingNumber;
    int PurchaseConfirmStatus;
}

