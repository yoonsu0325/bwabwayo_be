package com.bwabwayo.app.domain.notification.controller;

import com.bwabwayo.app.domain.notification.service.SseService;
import com.bwabwayo.app.domain.user.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final SseService sseService;

    @Operation(summary = "SSE 알림 구독", description = "로그인한 사용자가 SSE를 통해 실시간 알림을 구독합니다.")
    @ApiResponse(
            responseCode = "200"
            , description = "SSE 연결 성공"
            , content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)

    )
    @PostMapping(value="/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@Parameter(hidden = true) @LoginUser User user){
        return sseService.subscribe(user.getId());
    }

    @Operation(summary = "알림 송신", description = "사용자에게 알림을 보냅니다. (TEST)")
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestParam String receiverId, @RequestParam String message) {
        sseService.send(receiverId, message);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 알림 목록 조회", description = "내 알림 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Void> getNotifications(@Parameter(hidden = true) @LoginUser User user){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Operation(summary = "알림 삭제", description = "알림을 삭제합니다.")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId, @Parameter(hidden = true) @LoginUser User user){
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
