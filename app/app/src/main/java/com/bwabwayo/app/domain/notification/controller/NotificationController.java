package com.bwabwayo.app.domain.notification.controller;

import com.bwabwayo.app.domain.notification.service.SseService;
import com.bwabwayo.app.domain.user.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final SseService sseService;

    @Operation(summary = "SSE 알림 구독", description = "로그인한 사용자가 SSE를 통해 실시간 알림을 구독합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 연결 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping(value="/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@Parameter(hidden = true) @LoginUser User user){
        return sseService.subscribe(user.getId());
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(
            @LoginUser User user,
            @RequestParam String message
    ) {
        sseService.send(user.getId(), message);
        return ResponseEntity.ok().build();
    }
}
