package com.bwabwayo.app.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    Double rating;
}
