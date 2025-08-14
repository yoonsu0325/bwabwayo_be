package com.bwabwayo.app.domain.user.dto.request;

import com.bwabwayo.app.domain.user.domain.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {
    private Long saleId;
    private String buyerId;
    private String sellerId;
    private Long productId;
    private float rating;
    private List<Long> evaluationItemsId;

    public static Review fromEntity(ReviewRequest request){
        return Review.builder().
                buyerId(request.getBuyerId()).
                sellerId(request.getSellerId()).
                productId(request.getProductId()).
                rating(request.getRating()).
                build();
    }
}
