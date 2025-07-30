package com.bwabwayo.app.domain.user.handler;

import com.bwabwayo.app.domain.user.dto.request.CustomOAuth2User;
import com.bwabwayo.app.domain.user.dto.request.OAuth2UserRequest;
import com.bwabwayo.app.domain.user.utils.JWTUtils;
import com.bwabwayo.app.domain.user.config.JwtProperties;
import com.bwabwayo.app.domain.user.domain.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtils jwtUtils;
    private final JwtProperties jwtProperties;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("인증성공");
        CustomOAuth2User oath2user = (CustomOAuth2User) authentication.getPrincipal();
        OAuth2UserRequest user= oath2user.getUser();
        //AT 30분짜리 발행
        String accessToken = jwtUtils.createToken(user,jwtProperties.getAccessExpMinutes(), user.getRole());

        //PREUSER면 회원가입 폼으로 이동
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (user.getRole() == Role.PREUSER) {
            System.out.println("PREUSER");
            String redirectUrl = UriComponentsBuilder
                    .fromUriString("http://i13e202.p.ssafy.io:3000/signup")
                    .queryParam("accessToken", accessToken)
                    .queryParam("isNewUser", user.getRole() == Role.PREUSER)
                    .queryParam("id", user.getId())
                    .queryParam("email", user.getEmail())
                    .queryParam("profileImage", URLEncoder.encode(user.getProfileImage(), StandardCharsets.UTF_8))
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);
        }else { //AT/RT 토큰 발행 후 전달
            System.out.println("USER");
            // 가입된 유저 → AccessToken + RefreshToken 발급
            String refreshToken = jwtUtils.createToken(user, jwtProperties.getRefreshExpMinutes(), user.getRole()); // 7일

            // RefreshToken은 HttpOnly 쿠키로 전달
            ResponseCookie cookie = JWTUtils.createHttpOnlyCookie(refreshToken);
            response.setHeader("Set-Cookie", cookie.toString());
//            response.addHeader(jwtProperties.getHeader(), jwtProperties.getType() + accessToken); //헤더로 주는 방식

            String redirectUrl = UriComponentsBuilder
                    .fromUriString("http://i13e202.p.ssafy.io:3000/")
                    .queryParam("accessToken", accessToken)
                    .queryParam("isNewUser", false)
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);
        }
    }
}
