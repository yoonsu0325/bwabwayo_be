package com.bwabwayo.app.domain.user.utils;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER, ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_" + name(); // Spring Security 권한용
    }

    @Override
    public String toString() {
        return name(); // DB에 저장될 값은 그대로
    }
}
