package com.bwabwayo.app.domain.product.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerDTO {
    String id;
    String nickname;
    String bio;
    String profileImage;
    Integer score;
    Float rating;
    Long reviewCount;
}
