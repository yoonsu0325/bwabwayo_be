package com.bwabwayo.app.domain.notification.controller;
import com.bwabwayo.app.domain.auth.annotation.LoginUserId;
import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.notification.dto.NotificationDTO;
import com.bwabwayo.app.domain.notification.dto.request.InboxRequest;
import com.bwabwayo.app.domain.notification.dto.request.UpsertRequest;
import com.bwabwayo.app.domain.notification.dto.response.NotificationResponse;
import com.bwabwayo.app.domain.notification.dto.response.NotificationListResponse;
import com.bwabwayo.app.domain.notification.service.NotificationService;
import com.bwabwayo.app.domain.notification.service.SseService;
import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
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
    @GetMapping(value="/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            @LoginUserId String userId
    ){
        return sseService.subscribe(userId, lastEventId);
    }

    @Operation(summary = "인박스 알림 가져오기")
    @ApiResponse(responseCode = "200")
    @GetMapping
    public ResponseEntity<NotificationListResponse> getUnreadNotifications(@Valid InboxRequest request, @LoginUser User user){
        Page<NotificationDTO> notifications = notificationService.findInbox(user.getId(), PageRequest.of(0, request.getLimit()));
        List<NotificationResponse> dtos = notifications.getContent().stream().map(notificationService::build).toList();

        return ResponseEntity.ok(NotificationListResponse.of(dtos));
    }

    @Operation(summary = "알림 보내기")
    @PostMapping("/send")
    public ResponseEntity<?> upsertNotification(@RequestBody UpsertRequest request){
        sseService.upsertNotification(request);
        return ResponseEntity.ok(Map.of("result", "알림 전송"));
    }

    @Operation(summary = "알림 읽기")
    @PostMapping("/mark/{notificationId}")
    public ResponseEntity<?> markRead(@PathVariable Long notificationId, @LoginUser User user){
        notificationService.markRead(user.getId(), notificationId);
        return ResponseEntity.ok(Map.of("result", "알림 읽음: notificationId="+notificationId));
    }

    @Operation(summary = "힌트 보내기")
    @PostMapping("/hint")
    public ResponseEntity<?> upsertHint(@RequestBody UpsertRequest request){
        sseService.upsertHint(request);
        return ResponseEntity.ok(Map.of("result", "알림 전송"));
    }
}
