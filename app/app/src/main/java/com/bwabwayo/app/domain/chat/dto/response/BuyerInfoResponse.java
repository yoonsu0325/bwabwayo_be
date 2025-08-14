package com.bwabwayo.app.domain.chat.dto.response;

import com.bwabwayo.app.domain.user.domain.User;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class BuyerInfoResponse {

    private String id;

    private String nickname;

    private String profileImageUrl;

    public static BuyerInfoResponse from(User user, String profileImageUrl){
        return BuyerInfoResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(profileImageUrl)
                .build();
    }

}
