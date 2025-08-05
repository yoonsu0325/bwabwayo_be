package com.bwabwayo.app.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class UserDetailResponse {
    private String nickname;
    private String profileImage;
    private String bio;
    private String accountNumber;
    private String bankName;
    private String accountHolder;
}
