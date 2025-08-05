package com.bwabwayo.app.domain.user.controller;

import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.product.domain.Sale;
import com.bwabwayo.app.domain.product.service.SaleService;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.dto.request.UserDetailRequest;
import com.bwabwayo.app.domain.user.dto.response.UserInfoResponse;
import com.bwabwayo.app.domain.user.dto.response.UserOrderResponse;
import com.bwabwayo.app.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final SaleService saleService;

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

    @Transactional
    @PutMapping("/detail")
    public ResponseEntity<?> updateUserDetail(@LoginUser User user, @RequestBody UserDetailRequest request) {
        userService.updateUserDetail(request, user);
        return ResponseEntity.ok("");
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@LoginUser User user,
    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserOrderResponse> orders = saleService.getOrders(user.getId(), pageable);
        return ResponseEntity.ok(orders);
    }
}
