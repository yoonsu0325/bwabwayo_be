package com.bwabwayo.app.domain.notification.controller;

import com.bwabwayo.app.domain.notification.dto.response.NotificationListResponseDTO;
import com.bwabwayo.app.domain.notification.service.NotificationService;
import com.bwabwayo.app.domain.notification.service.SseService;
import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private final NotificationService notificationService;

    @Operation(summary = "SSE 알림 구독", description = "로그인한 사용자가 SSE를 통해 실시간 알림을 구독합니다.")
    @ApiResponse(
            responseCode = "200"
            , description = "SSE 연결 성공"
            , content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
    )
    @GetMapping(value="/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            @Parameter(hidden = true) @LoginUser User user
    ){
        return sseService.subscribe(user.getId(), lastEventId);
    }

    @Deprecated
    @Operation(summary = "[TEST] 알림 송신", description = "사용자에게 알림을 보냅니다.")
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestParam String receiverId, @RequestParam String message) {
        sseService.send(receiverId, message);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 알림 목록 조회", description = "내 알림 목록을 조회합니다.")
    @ApiResponse(
            responseCode = "200"
            , description = "내 알림 목록을 가져옵니다."
            , content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationListResponseDTO.class))
    )
    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestParam(required = false, defaultValue = "true") Boolean onlyUnread,
            @Parameter(hidden = true) @LoginUser User loginUser){
        NotificationListResponseDTO responseDTO = notificationService.getAllMyUnreadNotifications(loginUser.getId(), onlyUnread);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 처리합니다. 삭제되지 않습니다.")
    @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId, @Parameter(hidden = true) @LoginUser User user){
        notificationService.setReadByNotificationId(notificationId, user.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "모든 알림을 읽음 처리합니다.")
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(@Parameter(hidden = true) @LoginUser User user) {
        notificationService.setReadAll(user.getId());
        return ResponseEntity.ok().build();
    }
}
