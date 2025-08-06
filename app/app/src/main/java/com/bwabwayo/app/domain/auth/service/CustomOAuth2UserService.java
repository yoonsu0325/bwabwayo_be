package com.bwabwayo.app.domain.auth.service;

import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.auth.dto.request.OAuth2UserRequest;
import com.bwabwayo.app.domain.auth.userinfo.KakaoUserInfo;
import com.bwabwayo.app.domain.auth.userinfo.OAuth2UserInfo;
import com.bwabwayo.app.domain.auth.dto.request.CustomOAuth2User;
import com.bwabwayo.app.domain.user.domain.Role;
import com.bwabwayo.app.domain.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        //카카오 서버에 요청한 유저 정보 받아오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    //받은 데이터를 가공 및 로그인 성공 여부 확인
    private OAuth2User processOAuth2User(org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        }

        User userEntity =
                userService.findById(oAuth2UserInfo.getProviderId());
        OAuth2UserRequest user;
        if (userEntity == null) {
            user = new OAuth2UserRequest();
            user.setId(oAuth2UserInfo.getProviderId());
            user.setEmail(oAuth2UserInfo.getEmail());
            user.setProfileImage(oAuth2UserInfo.getProfileImage());
            user.setRole(Role.PREUSER);
        } else {
            user = new OAuth2UserRequest(userEntity);
            user.setRole(Role.USER);
        }
        return new CustomOAuth2User(user);
    }
}
