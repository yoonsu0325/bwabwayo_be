package com.bwabwayo.app.domain.user.controller;


import com.bwabwayo.app.domain.user.config.JwtProperties;
import com.bwabwayo.app.domain.user.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.user.dto.response.UserTokenResponse;
import com.bwabwayo.app.domain.user.repository.AccountRepository;
import com.bwabwayo.app.domain.user.repository.DeliveryAddressRepository;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.domain.user.service.AuthService;
import com.bwabwayo.app.domain.user.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JWTUtils jwtUtils;
    private final JwtProperties  jwtProperties;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserSignUpRequest request) {
        try {
            System.out.println("SignUp");
            UserTokenResponse tokens = authService.signUp(request);

            // AccessToken만 바디에 포함
            Map<String, String> responseBody = Map.of(
                    "accessToken", tokens.getAccessToken()
            );

            // RefreshToken은 HttpOnly 쿠키로 설정
            ResponseCookie refreshTokenCookie = JWTUtils.createHttpOnlyCookie(tokens.getRefreshToken());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(responseBody);
        } catch (DataIntegrityViolationException e) {
            // 중복 이메일, 닉네임, 전화번호 등 제약조건 위반
            return ResponseEntity.badRequest().body("중복된 정보가 있습니다.");
        } catch (IllegalArgumentException e) {
            // 서비스 레이어에서 유효성 검사 실패 등
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 그 외 예외 (로그 남기고 generic 응답)
            e.printStackTrace(); // 또는 log.error(...)
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

//    @PostMapping("/logout")
//    public ResponseEntity<?> logout() {
//
//    }

//    @PostMapping("/refresh")
//    public ResponseEntity<?> refreshToken() {
//
//    }
}
