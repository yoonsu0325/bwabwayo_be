package com.bwabwayo.app.domain.user.provider;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getEmail();
    String getProfileImage();
}
