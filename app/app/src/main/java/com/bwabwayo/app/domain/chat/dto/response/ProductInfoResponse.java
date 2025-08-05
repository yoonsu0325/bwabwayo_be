package com.bwabwayo.app.domain.chat.dto.response;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class ProductInfoResponse {

    private Long id;

    private String title;

    private Integer price;

    private String imageUrl;

    private SaleStatus saleStatus;

    private Integer shippingFee;

    private boolean canDelivery;

    private boolean canVideoCall;

    private boolean canDirect;

    private boolean canNegotiate;

    public static ProductInfoResponse from(Product product, String imageUrl){
        return ProductInfoResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .imageUrl(imageUrl)
                .saleStatus(product.getSaleStatus())
                .shippingFee(product.getShippingFee())
                .canDelivery(product.isCanDelivery())
                .canVideoCall(product.isCanVideoCall())
                .canDirect(product.isCanDirect())
                .canNegotiate(product.isCanNegotiate())
                .build();
    }
}
