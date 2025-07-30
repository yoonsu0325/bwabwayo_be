package com.bwabwayo.app.domain.user.config;

import com.bwabwayo.app.domain.user.filter.JWTFilter;
import com.bwabwayo.app.domain.user.handler.SuccessHandler;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.domain.user.service.CustomOAuth2UserService;
import com.bwabwayo.app.domain.user.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserRepository userRepository;
    private final JWTUtils jwtUtils;
    private final SuccessHandler successHandler;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        //Cors 설정 저장 객체
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        //cors 명시해서 허용시켜주기
        corsConfiguration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://localhost:3000",
                "http://localhost:3001",
                "https://localhost:3001",
                "http://localhost:8081",
                "https://i13e202.p.ssafy.io",
                "https://i13e202.p.ssafy.io/fe/",
                "https://i13e202.p.ssafy.io:3000",
                "https://i13e202.p.ssafy.io:3001"
        )); // ⭐ 정확한 origin 명시!

        //허용할 HTTP 메서드들을 명시
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));

        //요청 시 클라이언트가 보낼 수 있는 헤더를 명시
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        //쿠키 / Authorization 헤더 / 인증 정보를 포함한 요청을 허용할지 여부
        corsConfiguration.setAllowCredentials(true); // ✅ withCredentials를 위해 필수

        //클라이언트(브라우저)가 응답에서 읽을 수 있도록 허용할 커스텀 헤더
        corsConfiguration.addExposedHeader("Content-Disposition");
        corsConfiguration.addExposedHeader("Authorization");

        //모든 경로에 대해 위에서 정의한 CORS 규칙을 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }


    @Bean
    public SecurityFilterChain filterchain(HttpSecurity http) throws Exception {
        http
                //csrf, httpBasic, formLogin : disable
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())

                //cors 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //JWTFilter 추가
                .addFilterBefore(new JWTFilter(userRepository, jwtUtils), UsernamePasswordAuthenticationFilter.class)

                //session 미사용
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //인증요청 (허가)
                .authorizeHttpRequests(auth -> auth
                                .anyRequest().permitAll() //일단 전부 그냥 허용
//                        .requestMatchers("/api/**").permitAll() //일단 /api/는 전부 승인
//                        .anyRequest().authenticated() //그 외에는 승인 필요
                )

                //OAuth2Login 처리
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
//                        .failureHandler() //실패 핸들러도 나중에 추가
                        .successHandler(successHandler) //성공 핸들러도 나중에 추가
                )

                // 예외 처리: API 호출시 인증 실패하면 401 반환
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );
        return http.build();
    }
}
