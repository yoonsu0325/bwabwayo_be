package com.bwabwayo.app.domain.user.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class OAuth2UserResponse {
    String id;
    String role;
    String email;
    String profileImage;
}
