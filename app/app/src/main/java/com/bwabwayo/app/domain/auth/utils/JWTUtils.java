package com.bwabwayo.app.domain.auth.utils;

import com.bwabwayo.app.domain.auth.service.AuthRedisService;
import com.bwabwayo.app.domain.user.domain.Role;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import static org.apache.commons.codec.digest.DigestUtils.sha256;

@Component
@RequiredArgsConstructor
@Slf4j
@Getter
//JWT에 필요한 여러 함수를 만들어 놓은 Util 클래스
public class JWTUtils {
    private SecretKey secretKey;
    private final JwtProperties jwtProperties;
    private final AuthRedisService  authRedisService;

    //Bean 생성할 때 실행하는 생성자
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret()); // BASE64 디코딩
        this.secretKey = Keys.hmacShaKeyFor(keyBytes); // 서명용 HMAC 키 생성
    }

    public String getTokenFromHeader(String header) {
        return header.split(" ")[1];
    }

    //토큰 생성 함수 (유효시간, 역할을 받아서 생성) (AccessToken/RefreshToken 둘 다 이걸로 생성 가능)
    public String createToken(String id, long validTime, Role role, String type) {
        return Jwts.builder()
                .setHeader(Map.of("typ","JWT"))   // JWT 헤더 명시 (선택)
                .setSubject(id)                // sub 필드 → 주체, 일반적으로 user_id (RT는 tempId 넣을 예정)
                .claim("role", role)                  // 사용자 권한(roles) claim에 저장
                .claim("tokenType", type)                  // tokenType 저장해서 혼용 못하게 설정
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant())) // 토큰 생성 시각
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(validTime).toInstant())) // 토큰 만료 시각
                .signWith(secretKey) // 비밀키로 서명 (HS256)
                .compact(); // JWT 문자열 생성
    }

    public String generateTempId(String userId) {
        int maxAttempts = 3;

        try {
            for (int i = 0; i < maxAttempts; i++) {
                String raw = userId + ":" + UUID.randomUUID();
                byte[] hashBytes = sha256(raw); // SHA-256 해시
                String hash = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes); // 올바른 인코딩
                String key = "token:" + hash;

                if (!authRedisService.keyCheck(key)) {
                    return hash;
                }
            }
            throw new IllegalStateException("임시 ID 생성 실패: 충돌이 너무 많습니다.");
        } catch (Exception e) {
            log.error("임시 ID 생성 중 예외 발생: {}", e.getMessage(), e);
            return null; // 또는 Optional<String>으로 바꾸는 것도 가능
        }
    }

    //토큰 유효한지 체크 (만료, 위조, 서명, 등등) 함수
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 비밀키 설정
                    .build()
                    .parseClaimsJws(token); // 토큰 파싱 및 유효성 검증
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 토큰이 유효하지 않은 경우
        }
    }

    //JWT payload의 "sub"을 꺼내는 함수
    public String getSubject(String jwt) {
        try{
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject(); // sub 필드 꺼냄 (보통 userId)
        }
        catch (JwtException e){
            return null;
        }
    }

    //JWT payload의 "role"을 꺼내는 함수
    public Role getRole(String jwt) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody()
                    .get("role", Role.class); // role 필드 꺼냄
        } catch (JwtException e) {
            return null;
        }
    }

    // JWT payload의 "tokenType"을 꺼내는 함수
    public String getTokenType(String jwt) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody()
                    .get("tokenType", String.class); // type 필드 꺼냄
        } catch (JwtException e) {
            return null;
        }
    }

    public String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public static ResponseCookie createHttpOnlyCookie(String refreshToken) { // 수정
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)  // JavaScript로 접근 불가 (XSS 방지)
                .secure(true)  // HTTPS 연결에서만 전송
                .path("/api/auth/refresh") // 이 경로에 접근할 때만 자동 전송됨
                .maxAge(Duration.ofDays(7)) // 7일 동안 유지
                .sameSite("Strict") //일반적으로는 "Lax"사용, 다른 사이트에서 접근 금지
                .build();
    }
}
