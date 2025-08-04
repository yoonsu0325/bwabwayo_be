package com.bwabwayo.app.domain.user.controller;

import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getMyInfo(@LoginUser User user) {
        return ResponseEntity.ok(userService.getUserInfo(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

}
