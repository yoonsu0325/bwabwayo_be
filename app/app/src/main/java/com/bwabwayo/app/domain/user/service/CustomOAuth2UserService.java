package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.dto.request.OAuth2UserRequest;
import com.bwabwayo.app.domain.user.userinfo.KakaoUserInfo;
import com.bwabwayo.app.domain.user.userinfo.OAuth2UserInfo;
import com.bwabwayo.app.domain.user.dto.request.CustomOAuth2User;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.domain.user.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        //카카오 서버에 요청한 유저 정보 받아오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        //받아온 정보 읽어보기
        System.out.println(oAuth2User);
        System.out.println("OAuth2UserService 함수 호출");
        // code를 통해 구성한 정보
        System.out.println("userRequest clientRegistration : " + userRequest.getClientRegistration());
        // token을 통해 응답받은 회원정보
        System.out.println("oAuth2User : " + oAuth2User);
        System.out.println("oAuth2User.getAttributes() : " + oAuth2User.getAttributes());

        return processOAuth2User(userRequest, oAuth2User);
    }

    //받은 데이터를 가공 및 로그인 성공 여부 확인
    private OAuth2User processOAuth2User(org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            System.out.println("카카오 로그인 요청~~");
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else {
            System.out.println("카카오만 지원 ㅎㅎ"); //지워도 됨
        }

        Optional<User> userEntity =
                userRepository.findById(oAuth2UserInfo.getProviderId());
        OAuth2UserRequest user;
        if (userEntity.isEmpty()) {
            System.out.println("존재X");
            user = new OAuth2UserRequest();
            user.setId(oAuth2UserInfo.getProviderId());
            user.setEmail(oAuth2UserInfo.getEmail());
            user.setProfileImage(oAuth2UserInfo.getProfileImage());
            user.setRole(Role.PREUSER);
        } else {
            System.out.println("존재");
            user = new OAuth2UserRequest(
                    userEntity.orElseThrow(() -> new RuntimeException("User not found"))
            );
            user.setRole(Role.USER);
        }
        return new CustomOAuth2User(user);
    }
}
