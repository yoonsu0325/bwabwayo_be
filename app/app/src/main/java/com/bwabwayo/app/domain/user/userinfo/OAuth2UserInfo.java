package com.bwabwayo.app.domain.user.userinfo;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getEmail();
    String getProfileImage();
}
