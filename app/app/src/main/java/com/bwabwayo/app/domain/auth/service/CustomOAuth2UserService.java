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
import org.springframework.security.oauth2.core.OAuth2Error;
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
        log.info("유저 정보 받아오기 성공");
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    //받은 데이터를 가공 및 로그인 성공 여부 확인
    private OAuth2User processOAuth2User(org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            log.info("카카오 정보 가져오기");
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_provider"), // 에러 코드
                    "지원하지 않는 OAuth2 Provider: " + userRequest.getClientRegistration().getRegistrationId()
            );
        }

        User userEntity =
                userService.findById(oAuth2UserInfo.getProviderId());
        OAuth2UserRequest user;
        log.info("userEntity : {}", userEntity);
        //null이면 새가입자, isActive가 false면 재가입자
        if (userEntity == null || !userEntity.isActive()) {
            log.info("새가입자 혹은 재가입자");
            user = new OAuth2UserRequest();
            user.setId(oAuth2UserInfo.getProviderId());
            user.setEmail(oAuth2UserInfo.getEmail());
            user.setProfileImage(oAuth2UserInfo.getProfileImage());
            user.setRole(Role.PREUSER);
        } else { //탈퇴하지 않은 기본유저
            log.info("기본유저");
            user = new OAuth2UserRequest(userEntity);
            user.setRole(Role.USER);
        }
        return new CustomOAuth2User(user);
    }
}
