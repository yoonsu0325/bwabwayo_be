package com.bwabwayo.app.domain.auth.annotation;

import com.bwabwayo.app.domain.auth.utils.JwtProperties;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.auth.exception.UnauthorizedException;
import com.bwabwayo.app.domain.auth.utils.JWTUtils;
import com.bwabwayo.app.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

//어노테이션을 설정했을 때 자동으로 주입해주는 클래스
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserService userService;
    private final JWTUtils jwtUtils;
    private final JwtProperties jwtProperties;
    public LoginUserArgumentResolver(UserService userService, JWTUtils jwtUtils, JwtProperties jwtProperties) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.jwtProperties = jwtProperties;
    }


    @Override //적용대상인지 확인
    public boolean supportsParameter(MethodParameter parameter) {
        //해당 파라미터에 @LoginUser가 붙었는가
        boolean hasLoginUserAnnotation = parameter.hasParameterAnnotation(LoginUser.class);

        //해당 파라미터의 타입이 User 혹은 User의 하위 클래스인가
        boolean assignableFrom = User.class.isAssignableFrom(parameter.getParameterType());

        //return값이 true라면 아래의 resolveArgument로 이동
        return hasLoginUserAnnotation && assignableFrom;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        LoginUser loginUser = parameter.getParameterAnnotation(LoginUser.class);

        // 웹 요청 객체에서 HttpServletRequest를 얻어와야 헤더를 읽을 수 있어서 가져오기
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        // 헤더에서 AccessToken 추출
        String accessToken = jwtUtils.getTokenFromHeader(request.getHeader("Authorization"));
        // 인증이 안된 사용자 (예외 발생), (사실, jwtFilter와 SecurityFilter로 인증 안된 사용자는 걸러지긴 함, 그래도 이중체크)
        if(accessToken == null) {
            if(loginUser.required()) {
                throw new UnauthorizedException("AccessToken이 존재하지 않습니다. 인증에 실패하였습니다.");
            }
            return null;
        }
        //AccessToken 여부 확인
        String type = jwtUtils.getTokenType(accessToken);
        if(type == null || !type.equals(jwtProperties.getTypeAccess())){
            if(loginUser.required()) {
                throw new UnauthorizedException("AcessToken이 아닙니다. 인증에 실패하였습니다.");
            }
            return null;
        }

        // AccessToken에서 userId 가져오기
        String userId = jwtUtils.getSubject(accessToken);
        // userId 존재여부 확인
        if(userId == null) {
            if(loginUser.required()) {
                throw new UnauthorizedException("userId가 존재하지 않습니다. 인증에 실패하였습니다.");
            }
            return null;
        }
        return userService.findById(userId);
    }
}