package com.bwabwayo.app.domain.auth.controller;


import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.auth.utils.EncryptUtil;
import com.bwabwayo.app.domain.user.service.UserService;
import com.bwabwayo.app.domain.auth.utils.JwtProperties;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.auth.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.auth.dto.response.UserTokenResponse;
import com.bwabwayo.app.domain.auth.service.AuthService;
import com.bwabwayo.app.domain.auth.service.AuthRedisService;
import com.bwabwayo.app.domain.auth.utils.JWTUtils;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final JWTUtils jwtUtils;
    private final JwtProperties  jwtProperties;
    private final AuthRedisService authRedisService;
    private final EncryptUtil encryptUtil;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "OAuth2 로그인 후, 추가 회원 정보를 입력 받아 가입 처리 후 토큰을 발급합니다.")
    public ResponseEntity<?> signup(@RequestBody UserSignUpRequest request) {
        try {
            UserTokenResponse tokens = authService.signUp(request);

            // RefreshToken은 HttpOnly 쿠키로 설정
            ResponseCookie refreshTokenCookie = JWTUtils.createHttpOnlyCookie(tokens.getRefreshToken());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(Map.of(
                            "accessToken", tokens.getAccessToken(),
                            "loginPoint", tokens.getLoginPoint(),
                            "signUpPoint", tokens.getSignUpPoint()
                    ));
        } catch (DataIntegrityViolationException e) {
            // 중복 이메일, 닉네임, 전화번호 등 제약조건 위반
            String message = e.getMostSpecificCause().getMessage();

            if (message.contains("unique_user_nickname")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "field", "nickname",
                        "message", "이미 사용 중인 닉네임입니다."
                ));
            } else if (message.contains("unique_user_email")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "field", "email",
                        "message", "이미 등록된 이메일입니다."
                ));
            } else if (message.contains("unique_user_phone")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "field", "phoneNumber",
                        "message", "이미 등록된 전화번호입니다."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "회원정보 중복 오류입니다."
                ));
            }
        } catch (IllegalArgumentException e) {
            // 서비스 레이어에서 유효성 검사 실패 등
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 그 외 예외 (로그 남기고 generic 응답)
            e.printStackTrace(); // 또는 log.error(...)
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    @Transactional
    @PostMapping("/refresh/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        //RT가 Redis에 있으면 삭제
        authService.deleteRefreshTokenFromRequest(request);

        // RT 쿠키 제거
        ResponseCookie expiredCookie = authService.generateExpiredRefreshTokenCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ResponseEntity.ok("로그아웃 완료");
    }

    //기본유저 로그인 시 RefreshToken 받기
    @PostMapping("/refresh/init")
    public ResponseEntity<?> refreshTokenInit(@LoginUser User user, HttpServletRequest request, HttpServletResponse response) {
        //user의 id를 가져와서 토큰 생성
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자가 없습니다.");
        }
        boolean isActive = user.isActive();
        if (!isActive) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비활성화된 사용자입니다.");
        }

        // 기존 refreshToken이 있다면 삭제 처리 (logout과 같은 로직)
        authService.deleteRefreshTokenFromRequest(request);
        ResponseCookie expiredCookie = authService.generateExpiredRefreshTokenCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        //RT 토큰을 HttpOnlyCookie로 만들어서 response에 담아서 전송
        String refreshToken;
        String tempId;
        try {
            tempId = jwtUtils.generateTempId(user.getId());
            if(tempId == null){
                throw new IllegalStateException("임시 ID 생성 실패: UUID 충돌 또는 내부 오류");
            }
            refreshToken = jwtUtils.createToken(tempId, jwtProperties.getRefreshExpMinutes(), user.getRole(), jwtProperties.getTypeRefresh());
        } catch (Exception e) {
            log.error("RefreshToken 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("토큰 생성 실패");
        }

        // ✅ RT를 Redis에 저장 (TTL: 7일)
        try {
            authRedisService.saveRefreshToken(tempId, user.getId(), refreshToken);
        } catch (Exception e) {
            log.error("Redis 저장 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Redis 저장 실패");
        }

        // RefreshToken은 HttpOnly 쿠키로 전달
        ResponseCookie cookie = JWTUtils.createHttpOnlyCookie(refreshToken);
        response.setHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok().build();
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String refreshToken = jwtUtils.extractRefreshTokenFromCookies(request);
        //refreshToken 유효성 검사
        if (refreshToken == null || !jwtUtils.validateToken(refreshToken)) {
            //401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 리프레시 토큰입니다.");
        }

        //tempId 유효성 검사
        String tempId = jwtUtils.getSubject(refreshToken);
        if (tempId == null) {
            //401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰에서 사용자 정보를 가져올 수 없습니다.");
        }

        //tempId 유효성 검사
        String type = jwtUtils.getTokenType(refreshToken);
        if (type == null ||  !type.equals(jwtProperties.getTypeRefresh())) {
            //401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 아닙니다.");
        }

        //DB에서 조회
        Object savedRefreshToken = authRedisService.getRefreshToken(tempId); //암호화된 RefreshToekn
        if (savedRefreshToken == null || !savedRefreshToken.equals(encryptUtil.encrypt(refreshToken))) {
            // 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("허용하지 않는 리프레시 토큰입니다.");
        }

        String userId = authRedisService.getDecryptedUserId(tempId);
        User user = userService.findById(userId);

        boolean isActive = user.isActive();
        if (!isActive) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비활성화된 사용자입니다.");
        }

        String newAccessToken = jwtUtils.createToken(userId, jwtProperties.getAccessExpMinutes(), user.getRole(), jwtProperties.getTypeAccess());

        // 리프레시 토큰 남은 시간 계산
        Date expiration = Jwts.parserBuilder().setSigningKey(jwtUtils.getSecretKey()).build().parseClaimsJws(refreshToken).getBody().getExpiration();
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();
        long thresholdMillis = jwtProperties.getRefreshReissueThresholdDays() * 24 * 60 * 60 * 1000L;

        boolean refreshReissued = false;

        if (remainingMillis <= thresholdMillis) {
            try {
                authRedisService.deleteRefreshToken(tempId); // 기존 refreshToken 삭제
                String newTempId = jwtUtils.generateTempId(userId);
                String newRefreshToken = jwtUtils.createToken(newTempId, jwtProperties.getRefreshExpMinutes(), user.getRole(), jwtProperties.getTypeRefresh());
                authRedisService.saveRefreshToken(newTempId, userId, newRefreshToken);
                ResponseCookie refreshTokenCookie = JWTUtils.createHttpOnlyCookie(newRefreshToken);
                response.setHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
                refreshReissued = true;
            } catch (Exception e) {
                log.error("RefreshToken 재발급 실패", e);
            }
        }

        Map<String, Object> responseBody = Map.of(
                "accessToken", newAccessToken,
                "message", refreshReissued
                        ? "AccessToken과 RefreshToken이 모두 재발급되었습니다."
                        : "AccessToken이 재발급되었습니다."
        );

        return ResponseEntity.ok()
                .body(responseBody);
    }
}
