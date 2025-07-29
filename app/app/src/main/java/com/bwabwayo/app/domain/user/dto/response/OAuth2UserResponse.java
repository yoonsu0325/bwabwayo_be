package com.bwabwayo.app.domain.user.dto.response;

import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.utils.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OAuth2UserResponse {
    String id;
    Role role;
    String email;
    String profileImage;
    public OAuth2UserResponse(User user){
        this.id = user.getId();
        this.role = user.getRole();
        this.email = user.getEmail();
        this.profileImage = user.getProfileImage();
    }
}
