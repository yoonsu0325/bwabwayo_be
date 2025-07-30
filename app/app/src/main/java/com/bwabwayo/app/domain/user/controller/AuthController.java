package com.bwabwayo.app.domain.user.controller;


import com.bwabwayo.app.domain.user.annotation.LoginUser;
import com.bwabwayo.app.domain.user.config.JwtProperties;
import com.bwabwayo.app.domain.user.domain.Role;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.dto.request.OAuth2UserRequest;
import com.bwabwayo.app.domain.user.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.user.dto.response.UserTokenResponse;
import com.bwabwayo.app.domain.user.repository.AccountRepository;
import com.bwabwayo.app.domain.user.repository.DeliveryAddressRepository;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.domain.user.service.AuthService;
import com.bwabwayo.app.domain.user.utils.JWTUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

import java.util.Arrays;
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
    @Operation(summary = "회원가입", description = "OAuth2 로그인 후, 추가 회원 정보를 입력 받아 가입 처리 후 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류 또는 중복된 정보"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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
            System.out.println("Set-Cookie: " + refreshTokenCookie.toString());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(responseBody);
        } catch (DataIntegrityViolationException e) {
            // 중복 이메일, 닉네임, 전화번호 등 제약조건 위반
            return ResponseEntity.badRequest().body("SQL 오류");
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
        //Cookie를 가져와서 거기서 refreshToken을 찾고 mapping한 뒤에 첫번째거 들고와서
        //있으면 그대로 받고 없으면 null값
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        //refreshToken이 있는 지 체크
        if (refreshToken != null) {
            System.out.println("refreshToken : " + refreshToken);
            String userId = jwtUtils.getSubject(refreshToken); // 서명 검증 없이 claim 추출만
            System.out.println("userId : " + userId);
            //refreshToken 안에 userId 있는 지 체크
            if (userId != null) {
                userRepository.findById(userId).ifPresent(user -> {
                    user.setRefreshToken(null);
                    userRepository.save(user);
                });
            }
        }

        // RT 쿠키 제거
        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .path("/api/auth/refresh")
                .maxAge(0)
                .httpOnly(true)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ResponseEntity.ok("로그아웃 완료");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        //refreshToken 유효성 검사
        if (refreshToken == null || !jwtUtils.validateToken(refreshToken)) {
            //401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 리프레시 토큰입니다.");
        }

        //user 유효성 검사
        String userId = jwtUtils.getSubject(refreshToken);
        if (userId == null) {
            //401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰에서 사용자 정보를 가져올 수 없습니다.");
        }

        //DB의 RT와 비교
        if(!userRepository.findByIdAndRefreshToken(userId, refreshToken).isPresent()){
            //401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("허용하지 않는 리프레시 토큰입니다.");
        }
        System.out.println("refresh의 refreshToken" + refreshToken);
        User user = userRepository.findById(userId).get();

        //아래에 주석 처리 된건, RT의 남은 시간이 규정한 시간보다 적게 되면 RT를 재발급해주는 로직을 위해 남겨둠
        Role role = user.getRole();
        OAuth2UserRequest oauth2 = new OAuth2UserRequest(userId, role, "", "");
        String newAccessToken = jwtUtils.createToken(oauth2, jwtProperties.getAccessExpMinutes(), role);
//        String newRefreshToken = jwtUtils.createToken(oauth2, jwtProperties.getRefreshExpMinutes(), role);

        // AccessToken만 바디에 포함
        Map<String, String> responseBody = Map.of(
                "accessToken", newAccessToken,
                "message", "AccessToken이 재발급되었습니다."
        );

        // RefreshToken은 HttpOnly 쿠키로 설정
//        ResponseCookie refreshTokenCookie = JWTUtils.createHttpOnlyCookie(newRefreshToken);

        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(responseBody);
    }
}
