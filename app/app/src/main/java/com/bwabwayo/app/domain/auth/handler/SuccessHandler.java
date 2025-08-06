package com.bwabwayo.app.domain.auth.handler;

import com.bwabwayo.app.domain.auth.dto.request.CustomOAuth2User;
import com.bwabwayo.app.domain.auth.dto.request.OAuth2UserRequest;
import com.bwabwayo.app.domain.auth.service.AuthRedisService;
import com.bwabwayo.app.domain.auth.utils.JWTUtils;
import com.bwabwayo.app.domain.auth.utils.JwtProperties;
import com.bwabwayo.app.domain.user.domain.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final AuthRedisService authRedisService;
    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("인증 성공");
        CustomOAuth2User oath2user = (CustomOAuth2User) authentication.getPrincipal();
        OAuth2UserRequest user= oath2user.getUser();
        //AT 30분짜리 발행
        String accessToken = jwtUtils.createToken(user.getId() ,jwtProperties.getAccessExpMinutes(), user.getRole(), jwtProperties.getTypeAccess());

        //PREUSER면 회원가입 폼으로 이동
        // 프론트에 JSON 응답으로 전달
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (user.getRole() == Role.PREUSER) {
            String redirectUrl = UriComponentsBuilder
                    .fromUriString("https://i13e202.p.ssafy.io/fe/logincallback")
                    .queryParam("accessToken", accessToken)
                    .queryParam("isNewUser", user.getRole() == Role.PREUSER)
                    .queryParam("id", user.getId())
                    .queryParam("email", user.getEmail())
                    .queryParam("profileImage", URLEncoder.encode(user.getProfileImage(), StandardCharsets.UTF_8))
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);
        }else { //AT 토큰 발행 후 전달, RT를 여기서 발급하면 kakao쪽으로 응답이 가버림

            String redirectUrl = UriComponentsBuilder
                    .fromUriString("https://i13e202.p.ssafy.io/fe/logincallback")
                    .queryParam("accessToken", accessToken)
                    .queryParam("isNewUser", false)
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);
        }
    }
}
