package com.bwabwayo.app.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserTokenResponse {
    private String accessToken;
    private String refreshToken;
}
