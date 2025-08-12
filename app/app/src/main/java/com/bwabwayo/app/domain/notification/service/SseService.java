package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${sse.timeout}")
    private Long timeout;

    /**
     * 사용자에게 알림을 보낼 수 있도록 등록
     */
    public SseEmitter subscribe(String userId) {
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
        if(old != null) old.complete();

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

        return emitter;
    }

    @Transactional
    public List<Notification> getRecentNotifications(String userId, String lastEventId){
        if(lastEventId != null) { // 연결이 끊긴 이후부터 조회
            long millis = Long.parseLong(lastEventId);
            LocalDateTime lastTime = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();

            return notificationRepository
                    .findAllByReceiverIdAndIsReadFalseAndCreatedAtAfter(userId, lastTime);
        } else{ // 전체에서 조회
            return notificationRepository
                    .findAllByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        }
    }

    // 알림 발송
    @Transactional
    public void pop(String userId, String lastEventId) {
        List<Notification> notifications = getRecentNotifications(userId, lastEventId);

        SseEmitter emitter = emitters.get(userId);
        if(emitter == null) return;
        if(notifications == null || notifications.isEmpty()) return;

        for (Notification n : notifications) {
            sendOne(emitter, userId,n);
        }

    }

    private long toEpochMilli(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
    }

    public void send(String userId, Notification notification) {
        SseEmitter emitter = emitters.get(userId);
        sendOne(emitter, userId, notification);
    }

    public void sendOne(SseEmitter emitter, String userId, Notification notification){
        if(emitter == null) return;

        try {
            log.info("알림 전송: notificationId={}", notification.getId());

            emitter.send(SseEmitter.event()
                    .id(String.valueOf(toEpochMilli(notification.getCreatedAt())))
                    .name("notification")
                    .data(notificationService.build(notification))
                    .reconnectTime(3000));
        } catch (IOException e) {
            emitters.remove(userId);
            emitter.completeWithError(e);
            throw new RuntimeException("SSE 알림 전송 중 예외 발생");
        }
    }
}
