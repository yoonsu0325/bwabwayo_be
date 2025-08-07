package com.bwabwayo.app.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserTokenResponse {
    private String accessToken;
    private String refreshToken;
    private int loginPoint;
    private int signUpPoint;
}
