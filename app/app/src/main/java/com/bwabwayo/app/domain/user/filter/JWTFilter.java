package com.bwabwayo.app.domain.user.filter;

import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.domain.user.utils.JWTUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.AuthenticationException;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final JWTUtils jwtUtils;

    private final String[] whitelist = {
            "/", "/login", "/join",
            "/auth/kakao",               // ✅ 이거 추가!!
            "/api/auth", "/api/auth/kakao",
            "/user/login/kakao", "/api/login/oauth2/code/kakao",
            "/api/auth/reissue",
            "/api-docs",
            "/swagger-ui-custom.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/api-docs/**",
            "/css/**",
            "/img/**",
            "/favicon.ico"
    };

    private void checkAuthorizationHeader(String header) {
        if(header == null) {
            throw new RuntimeException("토큰이 전달되지 않았습니다");
        } else if (!header.startsWith("Bearer ")) {
            throw new RuntimeException("BEARER 로 시작하지 않는 올바르지 않은 토큰 형식입니다");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();

        // ✅ 명시적으로 /login은 무조건 필터 제외
        return uri.equals("/login") ||
                uri.equals("/user/login") ||
                uri.equals("/join") ||
                uri.startsWith("/auth/") ||
                uri.startsWith("/oauth2/") ||
                uri.startsWith("/api/login/oauth2/") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/img/") ||
                uri.equals("/") ||
                uri.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String uri = request.getRequestURI();
        System.out.println("🔥 TokenAuthFilter: " + uri);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            checkAuthorizationHeader(authHeader);   // header 가 올바른 형식인지 체크
            Authentication authentication = getAuthentication(authHeader);

            log.info("authentication = {}", authentication);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);    // 다음 필터로 이동
        } catch (Exception e) {
            response.setContentType("application/json; charset=UTF-8");

            if (e instanceof ExpiredJwtException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token_Expired: " + e.getMessage());
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: " + e.getMessage());
            }

        }
    }

    private Authentication getAuthentication(String authHeader) throws AuthenticationException {
        System.out.println(authHeader);
        String tokenFromHeader = TokenProvider.getTokenFromHeader(authHeader);
        String claims = TokenProvider.getClaims(tokenFromHeader);
        if(claims == null) throw new AuthenticationException("토큰값이 잘못되었습니다");
        Member member = memberService.findMemberById(claims);
        PrincipalDetail principalDetail = new PrincipalDetail(member);
        System.out.println(principalDetail.getName());
        return new UsernamePasswordAuthenticationToken(principalDetail, "", principalDetail.getAuthorities());
        return new UsernamePasswordAuthenticationToken();
    }
}
