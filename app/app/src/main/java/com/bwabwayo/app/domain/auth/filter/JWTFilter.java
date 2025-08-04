package com.bwabwayo.app.domain.auth.filter;

import com.bwabwayo.app.domain.auth.utils.JwtProperties;
import com.bwabwayo.app.domain.user.domain.Role;
import com.bwabwayo.app.domain.auth.dto.request.CustomOAuth2User;
import com.bwabwayo.app.domain.auth.dto.request.OAuth2UserRequest;
import com.bwabwayo.app.domain.auth.utils.JWTUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.AuthenticationException;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtils jwtUtils;
    private final JwtProperties jwtProperties;

    //Header 유효성 체크
    private void checkAuthorizationHeader(String header) {
        if (header == null) {
            throw new BadCredentialsException("토큰이 전달되지 않았습니다");
        } else if (!header.startsWith("Bearer ")) {
            throw new BadCredentialsException("BEARER로 시작하지 않는 잘못된 토큰 형식입니다");
        }
    }

    //필터링 되면 안되는 API들
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();

        // ✅ 명시적으로 /login은 무조건 필터 제외
        return uri.equals("/login") ||
                (uri.startsWith("/api/auth/") && !uri.equals("/api/auth/signup")) ||
                uri.startsWith("/oauth2/") ||
                uri.startsWith("/api/") ||
                uri.startsWith("/be/") ||
                uri.startsWith("/v3/") ||
                uri.startsWith("/api/login/oauth2/") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/img/") ||
                uri.startsWith("/swagger-ui/") ||
                uri.startsWith("/v3/api-docs") ||
                uri.equals("/") ||
                uri.equals("/favicon.ico") ||
                uri.startsWith("/static/") ||
                uri.startsWith("/assets/") ||
                uri.startsWith("/ws-stomp/") ||
                uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".png") || uri.endsWith(".jpg");
        }

    //Filter에서 실행되는 함수
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String uri = request.getRequestURI();
        System.out.println("🔥 TokenAuthFilter: " + uri);
        try {
            checkAuthorizationHeader(authHeader);   // header 가 올바른 형식인지 체크
            //Header로부터 authentication 가져오기
            Authentication authentication = getAuthentication(authHeader);

            //로그
            log.info("authentication = {}", authentication);

            //SecuritycontextHolder에 넣어서 다음 인증 때 사용
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("Token 인증 완료");
            filterChain.doFilter(request, response);    // 다음 필터로 이동
        } catch (Exception e) {
            response.setContentType("application/json; charset=UTF-8");

            if (e instanceof ExpiredJwtException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            } else if (e instanceof BadCredentialsException || e instanceof AuthenticationException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
            }
        }
    }

    private Authentication getAuthentication(String authHeader) throws AuthenticationException {
        System.out.println(authHeader);

        String tokenFromHeader = jwtUtils.getTokenFromHeader(authHeader);
        String userId = jwtUtils.getSubject(tokenFromHeader); // 실제로 subject를 반환함

        //Id값 체크, null이라면 만료된 토큰
        if (userId == null) {
            throw new BadCredentialsException("토큰값이 잘못되었습니다");
        }
        String type = jwtUtils.getTokenType(tokenFromHeader);
        if (type == null || !type.equals(jwtProperties.getTypeAccess())){
            throw new AccessDeniedException("Access 토큰이 아닙니다.");
        }
        //User -> CustomOAuth2User로 가공
        Role role = jwtUtils.getRole(tokenFromHeader);
        OAuth2UserRequest oauth2user = new OAuth2UserRequest(userId, role, "", "");
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oauth2user);

        System.out.println(customOAuth2User.getName());

        return new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                customOAuth2User.getAuthorities()
        );
    }
}
