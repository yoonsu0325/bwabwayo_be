package com.bwabwayo.app.domain.notification.controller;
import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.notification.dto.request.UpsertRequest;
import com.bwabwayo.app.domain.notification.dto.response.NotificationDTO;
import com.bwabwayo.app.domain.notification.dto.response.NotificationListResponseDTO;
import com.bwabwayo.app.domain.notification.service.NotificationService;
import com.bwabwayo.app.domain.notification.service.SseService;
import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
            @LoginUser User user
    ){
        return sseService.subscribe(user.getId(), lastEventId);
    }

    @Operation(summary = "인박스 알림 가져오기")
    @ApiResponse(responseCode = "200")
    @GetMapping
    public ResponseEntity<?> getUnreadNotifications(
            @LoginUser User user,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "3") Integer size
        ){
        Page<Notification> notifications = notificationService.findInbox(user.getId(), PageRequest.of(page, size));
        List<NotificationDTO> dtos = notifications.getContent().stream().map(notificationService::build).toList();

        return ResponseEntity.ok(NotificationListResponseDTO.of(dtos));
    }

    @Operation(summary = "채팅방 알림/메시지 보내기")
    @PostMapping("/send/chat/{chatId}")
    public ResponseEntity<?> upsertChatNotification(
            @PathVariable Long chatId,
            @RequestBody UpsertRequest request
    ){
        sseService.upsertChatNotification(chatId, request);
        return ResponseEntity.ok(Map.of("result", "채팅 알림 전송"));
    }

    @Operation(summary = "상품 알림 보내기")
    @PostMapping("/send/product/{productId}")
    public ResponseEntity<?> upsertProductNotification(
            @PathVariable Long productId,
            @RequestBody UpsertRequest request
    ){
        sseService.upsertProductNotification(productId, request);
        return ResponseEntity.ok(Map.of("result", "상품 알림 전송"));
    }

    @Operation(summary = "채팅방 알림/메시지 읽기")
    @PostMapping("/mark/chat/{chatId}")
    public ResponseEntity<?> markChatRead(
            @PathVariable Long chatId,
            @LoginUser User user
    ){
        notificationService.markChatRead(user.getId(), chatId);
        return ResponseEntity.ok(Map.of("result", "채팅 읽음 표시: chatId="+chatId));
    }

    @Operation(summary = "상품 알림 읽기")
    @PostMapping("/mark/product/{productId}")
    public ResponseEntity<?> markProductRead(
            @PathVariable Long productId,
            @LoginUser User user
    ){
        notificationService.markProductRead(user.getId(), productId);
        return ResponseEntity.ok(Map.of("result", "상품 읽음 표시: productId="+productId));
    }
}
