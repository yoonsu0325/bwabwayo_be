package com.bwabwayo.app.domain.auth.userinfo;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getEmail();
    String getProfileImage();
}
