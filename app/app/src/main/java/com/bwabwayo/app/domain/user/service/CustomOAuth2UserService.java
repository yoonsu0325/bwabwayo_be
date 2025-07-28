package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.provider.KakaoUserInfo;
import com.bwabwayo.app.domain.user.provider.OAuth2UserInfo;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);
        System.out.println("OAuth2UserService 함수 호출");
        // code를 통해 구성한 정보
        System.out.println("userRequest clientRegistration : " + userRequest.getClientRegistration());
        // token을 통해 응답받은 회원정보
        System.out.println("oAuth2User : " + oAuth2User);

        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            System.out.println("카카오 로그인 요청~~");
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else {
            System.out.println("우리는 구글과 페이스북만 지원해요 ㅎㅎ");
        }

        User userEntity =
                userRepository.findById(oAuth2UserInfo.getProviderId());

        if (userEntity != null) {
            //userEntity 이미 존재하므로 넘기기
            //login Successful
            //즉, SuccessfulHandling으로 넘겨서 성공시켜줘야함
        } else {
            //존재하지 않으므로 front에 요청해서 front로 넘기기
        }
    }
}
