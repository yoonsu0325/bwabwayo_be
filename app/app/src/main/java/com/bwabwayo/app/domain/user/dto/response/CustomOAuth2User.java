package com.bwabwayo.app.domain.user.dto.response;

import com.bwabwayo.app.domain.user.provider.OAuth2UserInfo;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {
    private final OAuth2UserResponse user;
    public CustomOAuth2User(OAuth2UserResponse user) {
        this.user = user;
    }
    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "user_id", user.getId(),
                "email", user.getEmail(),
                "profileImage", user.getProfileImage()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> user.getRole().getAuthority());
    }

    @Override
    public String getName() {
        return user.getId(); // Security 내부에서 호출됨
    }
}
