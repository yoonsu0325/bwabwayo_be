package com.bwabwayo.app.domain.user.controller;

import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.auth.dto.response.UserTokenResponse;
import com.bwabwayo.app.domain.auth.service.AuthService;
import com.bwabwayo.app.domain.auth.utils.JWTUtils;
import com.bwabwayo.app.domain.chat.dto.response.ReservationResponse;
import com.bwabwayo.app.domain.chat.service.ReservationService;
import com.bwabwayo.app.domain.product.service.SaleService;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.dto.request.UserDetailRequest;
import com.bwabwayo.app.domain.user.dto.response.UserOrderResponse;
import com.bwabwayo.app.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final SaleService saleService;
    private final AuthService authService;
    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<?> getMyInfo(@LoginUser User user) {
        return ResponseEntity.ok(userService.getUserInfo(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getUserDetail(@LoginUser User user) {
        return ResponseEntity.ok(userService.getUserDetail(user));
    }


    @PutMapping("/detail")
    public ResponseEntity<?> updateUserDetail(@LoginUser User user, @RequestBody UserDetailRequest request) {
        try {
            userService.updateUserDetail(request, user);
            return ResponseEntity.ok("회원 정보 수정 완료");
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

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@LoginUser User user,
    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserOrderResponse> orders = saleService.getOrders(user.getId(), pageable);
        return ResponseEntity.ok(orders);
    }

    @Transactional
    @DeleteMapping
    public ResponseEntity<?> deleteUser(@LoginUser User user, HttpServletRequest request) {
        userService.deleteUser(user, request);
        try {
            //RefreshToken 삭제
            authService.deleteRefreshTokenFromRequest(request);
        } catch (Exception e) {
            log.error("RefreshToken 삭제 실패: {}", e.getMessage(), e);
        }
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    @GetMapping("/video")
    public ResponseEntity<?> getAllReservation(
            @LoginUser User user
    ){
        List<ReservationResponse> list = reservationService.findAllReservations(user);
        return ResponseEntity.ok(list);
    }

}
