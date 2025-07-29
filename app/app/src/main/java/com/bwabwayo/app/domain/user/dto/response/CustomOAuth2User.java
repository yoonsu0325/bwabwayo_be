package com.bwabwayo.app.domain.user.dto.response;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    private OAuth2UserResponse user;
    private Map<String, Object> attributes;

    public CustomOAuth2User(OAuth2UserResponse user,  Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }
    public CustomOAuth2User(OAuth2UserResponse user) {
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> "ROLE_" + user.getRole());
    }

    @Override
    public String getName() {
        return user.getId();
    }
}
