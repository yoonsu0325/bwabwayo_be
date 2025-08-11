package com.bwabwayo.app.domain.product.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerDetailDTO {
    String id;
    String nickname;
    String bio;
    String profileImage;
    Integer score;
    Float rating;
    Long reviewCount;
    @Builder.Default
    List<ProductDTO> otherProducts = new ArrayList<>();
}
