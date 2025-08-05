package com.bwabwayo.app.domain.chat.dto.response;

import com.bwabwayo.app.domain.user.domain.ReviewAgg;
import com.bwabwayo.app.domain.user.domain.User;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class SellerInfoResponse {

    private String id;
    private String nickname;

    private String profileImageUrl;

    private int reviewCount;

    private float avgRating;

    private int dealCount;


    public static SellerInfoResponse from(User user, ReviewAgg reviewAgg){
        return SellerInfoResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImage())
                .reviewCount(reviewAgg.getReviewCount())
                .avgRating(reviewAgg.getAvgRating())
                .dealCount(user.getDealCount())
                .build();
    }

}
