package com.bwabwayo.app.domain.auth.service;

import com.bwabwayo.app.domain.user.service.AccountService;
import com.bwabwayo.app.domain.address.service.DeliveryAddressService;
import com.bwabwayo.app.domain.user.service.UserService;
import com.bwabwayo.app.domain.auth.utils.JwtProperties;
import com.bwabwayo.app.domain.user.domain.Role;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.auth.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.auth.dto.response.UserTokenResponse;
import com.bwabwayo.app.domain.auth.utils.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final AccountService accountService;
    private final DeliveryAddressService deliveryAddressService;
    private final JWTUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final AuthRedisService authRedisService;

    @Transactional
    public UserTokenResponse signUp(UserSignUpRequest request) {
        // 1. 필수 유저 정보 검증
        if (request.getId() == null || request.getNickname() == null) {
            throw new IllegalArgumentException("아이디 또는 닉네임이 누락되었습니다.");
        }
        Role role = Role.USER;
        String accessToken = jwtUtils.createToken(request.getId(), jwtProperties.getAccessExpMinutes(), role, jwtProperties.getTypeAccess());
        String tempId = jwtUtils.generateTempId(request.getId());
        if (tempId == null) {
            throw new IllegalStateException("임시 ID 생성 실패: UUID 충돌 또는 내부 오류");
        }
        String refreshToken = jwtUtils.createToken(tempId, jwtProperties.getRefreshExpMinutes(), role, jwtProperties.getTypeRefresh());
        // ✅ RT를 Redis에 저장 (TTL: 7일)
        authRedisService.saveRefreshToken(tempId, request.getId(), refreshToken);

        // 2. User 저장
        User user = userService.createUser(request);

        // 3. Account 저장 조건 검사
        if (request.getAccountNumber() != null &&
                request.getAccountHolder() != null &&
                request.getBankName() != null) {
            accountService.createAccount(user, request);
        }

        // 4. DeliveryAddress 저장 조건 검사
        if (request.getRecipientName() != null &&
                request.getRecipientPhoneNumber() != null &&
                request.getZipcode() != null &&
                request.getAddress() != null &&
                request.getAddressDetail() != null) {
            deliveryAddressService.createAddress(user, request);
        }

        // 5. 토큰 응답 반환
        return UserTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void deleteRefreshTokenFromRequest(HttpServletRequest request) {
        //Cookie를 가져와서 거기서 refreshToken을 찾고 mapping한 뒤에 첫번째거 들고와서
        //있으면 그대로 받고 없으면 null값

        String refreshToken = jwtUtils.extractRefreshTokenFromCookies(request);
        //refreshToken이 있는 지 체크
        if (refreshToken != null) {
            try {
                String tempId = jwtUtils.getSubject(refreshToken);
                if (tempId != null) {
                    authRedisService.deleteRefreshToken(tempId);
                }
            } catch (Exception e) {
                log.warn("기존 RefreshToken 삭제 실패", e);
            }
        }
    }

    public ResponseCookie generateExpiredRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .path("/api/auth/refresh")
                .maxAge(0)
                .httpOnly(true)
                .build();
    }
}

