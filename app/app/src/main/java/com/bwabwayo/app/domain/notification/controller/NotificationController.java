package com.bwabwayo.app.domain.notification.controller;
import com.bwabwayo.app.domain.notification.dto.response.NotificationListResponseDTO;
import com.bwabwayo.app.domain.notification.service.NotificationService;
import com.bwabwayo.app.domain.notification.service.SseService;
import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

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
            @LoginUser User user
    ){
//        return sseService.subscribe(user.getId(), lastEventId);
        return sseService.subscribe(user.getId());
    }

    @Operation(summary = "내 알림 목록 조회", description = "내 알림 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내 알림 목록을 가져옵니다.")
    @GetMapping
    public ResponseEntity<NotificationListResponseDTO> getNotifications(@LoginUser User loginUser){
        NotificationListResponseDTO responseDTO = notificationService.findAllByReceiverId(loginUser.getId());
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 처리합니다. 삭제되지 않습니다.")
    @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId, @LoginUser User user){
        notificationService.readNotification(notificationId, user.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "모든 알림을 읽음 처리합니다.")
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(@LoginUser User user) {
        notificationService.readAllNotifications(user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(
            @RequestParam String targetUserId,
            @RequestParam String message,
            @RequestParam Long chatroomId
    ){
        notificationService.send(targetUserId, message, chatroomId);
        
        return ResponseEntity.ok(Map.of("result", "알림 전송"));
    }
}
