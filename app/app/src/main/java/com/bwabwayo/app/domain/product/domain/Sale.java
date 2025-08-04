package com.bwabwayo.app.domain.product.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty("product_id")
    private Long productId;
    @JsonProperty("seller_id")
    private String sellerId;
    @JsonProperty("buyer_id")
    private String buyerId;
    @JsonProperty("sale_price")
    private Integer salePrice;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("is_reviewed")
    private boolean isReviewed;
    @JsonProperty("room_id")
    private Long roomId;
}
