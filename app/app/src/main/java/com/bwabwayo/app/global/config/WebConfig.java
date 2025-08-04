package com.bwabwayo.app.global.config;

import com.bwabwayo.app.domain.auth.annotation.LoginUserArgumentResolver;
import com.bwabwayo.app.domain.auth.utils.JWTUtils;
import com.bwabwayo.app.domain.auth.utils.JwtProperties;
import com.bwabwayo.app.domain.user.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserService userService;
    private final JWTUtils jwtUtils;
    private final JwtProperties jwtProperties;

    public WebConfig(UserService userService, JWTUtils jwtUtils, JwtProperties jwtProperties) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.jwtProperties = jwtProperties;
    }

    @Override
    //커스텀한 어노테이션 추가
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginUserArgumentResolver(userService, jwtUtils,  jwtProperties));
    }
}