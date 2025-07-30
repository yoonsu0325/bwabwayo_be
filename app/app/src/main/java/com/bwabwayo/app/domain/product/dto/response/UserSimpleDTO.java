package com.bwabwayo.app.domain.product.dto.response;

import com.bwabwayo.app.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 판매자 요약 정보 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSimpleDTO {
    private String id; // 판매자 ID
    private String nickname; // 판매자 닉네임


    public static UserSimpleDTO fromEntity(User user){
        return new UserSimpleDTO(user.getId(), user.getNickname());
    }
}
