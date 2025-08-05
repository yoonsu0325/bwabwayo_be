package com.bwabwayo.app.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class UserDetailResponse {
    private int status;
    private String message;
    String nickname;
    String profileImage;
    String bio;
    String accountNumber;
    String bankName;
    String accountHolder;
    public static UserDetailResponse of(String nickname, String profileImage, String bio, String account_number,
                                            String bank_name, String account_holder) {
        return UserDetailResponse.builder()
                .status(200)
                .message("회원 상세 정보 조회에 성공하였습니다")
                .nickname(nickname)
                .profileImage(profileImage)
                .bio(bio)
                .accountNumber(account_number)
                .bankName(bank_name)
                .accountHolder(account_holder)
                .build();
    }

}
