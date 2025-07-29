package com.bwabwayo.app.domain.user.dto.request;

import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserRequest {
    String id;
    Role role;
    String email;
    String profileImage;
    public OAuth2UserRequest(User user){
        this.id = user.getId();
        this.role = user.getRole();
        this.email = user.getEmail();
        this.profileImage = user.getProfileImage();
    }
}
