package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRepository;
import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.notification.dto.request.UpsertRequest;
import com.bwabwayo.app.domain.notification.dto.response.NotificationDTO;
import com.bwabwayo.app.domain.notification.repository.NotificationRepository;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final NotificationRepository notificationRepository;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ProductRepository productRepository;
    @Value("${sse.timeout}")
    private Long timeout;

    /**
     * 사용자에게 알림을 보낼 수 있도록 등록
     */
    @Transactional
    public SseEmitter subscribe(String userId, String lastEventId) {
        List<Notification> notifications = notificationService.findInbox(userId, PageRequest.of(0, 3)).getContent();

        SseEmitter emitter = new SseEmitter(timeout);

        try {
            // 연결 확인용 event 발송
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(System.currentTimeMillis()))
                    .name("connect")
                    .data("connected")
                    .reconnectTime(3000));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        // 이전 연결 제거
        SseEmitter old = emitters.put(userId, emitter);
        if (old != null) old.complete();

        // 연결종료, 타임아웃, 에러발생 시 emitter 제거 (메모리 누수 방지)
        log.info("SSE 구독 시작: userId={}", userId);
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE 연결 종료: userId={}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SSE 타임 아웃: userId={}", userId);
        });
        emitter.onError((e) -> {
            emitters.remove(userId);
            log.info("SSE 에러 발생: userId={}", userId);
        });

        for (Notification n : notifications) {
            pushEvent(emitter, userId, n);
        }
        return emitter;
    }

    @Transactional
    public List<Notification> getRecentNotifications(String userId, String lastEventId) {
        if (lastEventId != null) { // 연결이 끊긴 이후부터 조회
            long millis = Long.parseLong(lastEventId);
            LocalDateTime lastTime = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();

            return notificationRepository
                    .findAllByReceiverIdAndIsReadFalseAndUpdatedAtAfter(userId, lastTime);
        } else { // 전체에서 조회
            return notificationRepository
                    .findAllByReceiverIdAndIsReadFalseOrderByUpdatedAtDesc(userId);
        }
    }

    public void pushEvent(String userId, Notification notification){
        SseEmitter emitter = emitters.get(userId);
        pushEvent(emitter, userId, notification);
    }
    private void pushEvent(SseEmitter emitter, String userId, Notification notification){
        if(emitter == null) return;

        try {
            log.info("알림 전송: notificationId={}", notification.getId());

            emitter.send(SseEmitter.event()
                    .id(String.valueOf(toEpochMilli(notification.getUpdatedAt())))
                    .name("notification")
                    .data(notificationService.build(notification))
                    .reconnectTime(3000));
        } catch (IOException e) {
            emitters.remove(userId);
            emitter.completeWithError(e);
            throw new RuntimeException("SSE 알림 전송 중 예외 발생");
        }
    }

    private long toEpochMilli(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
    }

    @Transactional
    public void upsertChatNotification(Long chatId, UpsertRequest request){
        String receiverId = request.getReceiverId();
        String message = request.getMessage();

        notificationService.upsertChat(receiverId, chatId, message, 1);
        Notification notification = notificationService.findByChat(receiverId, chatId);

        pushEvent(receiverId, notification);
    }

    @Transactional
    public void upsertProductNotification(Long productId, UpsertRequest request){
        String receiverId = request.getReceiverId();
        String message = request.getMessage();

        notificationService.upsertProduct(receiverId, productId, message);
        Notification notification = notificationService.findByProduct(receiverId, productId);

        pushEvent(receiverId, notification);
    }
}
