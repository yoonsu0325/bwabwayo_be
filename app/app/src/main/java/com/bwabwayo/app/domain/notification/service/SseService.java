package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.notification.dto.response.NotificationDTO;
import com.bwabwayo.app.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
    @Value("${sse.timeout}")
    private Long timeout;


    /**
     * 사용자에게 알림을 보낼 수 있도록 등록
     */
    public SseEmitter subscribe(String userId, String lastEventId) {
//        try {
//            // 연결 확인용 event 발송
//            emitter.send(SseEmitter.event()
//                    .id(String.valueOf(System.currentTimeMillis()))
//                    .name("connect")
//                    .data("connected")
//                    .reconnectTime(3000));
//        } catch (IOException e) {
//            emitter.completeWithError(e);
//        }

        List<Notification> notifications = null;
        try {
            try{
                if(lastEventId != null){
                    long millis = Long.parseLong(lastEventId);
                    LocalDateTime lastTime = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .toLocalDateTime();

                    notifications = notificationRepository
                            .findAllByReceiverIdAndIsReadFalseAndCreatedAtAfter(userId, lastTime);
                }
            } catch (NumberFormatException e){
                log.warn("LastEventId의 형식이 올바르지 않습니다: LastEventId={}", lastEventId);
            } finally {
                if(notifications == null){
                    notifications = notificationRepository
                            .findAllByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId);
                }
            }
        } catch (Exception e) {
            log.warn("알림 복원 실패: {}", e.getMessage());
        }

        SseEmitter emitter = new SseEmitter(timeout);

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

        if(notifications != null) {
            try {
                for (Notification n : notifications) {
                    log.info("알림 전송: notificationId={}", n.getId());
                    emitter.send(SseEmitter.event()
                            .id(String.valueOf(toEpochMilli(n.getCreatedAt())))
                            .name("notification")
                            .data(NotificationDTO.fromEntity(n))
                    );
                }
            } catch (IOException e){
                    emitter.completeWithError(e);
            }
        }
        return emitter;
    }

    private long toEpochMilli(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
    }

    public void send(String userId, Notification notification) {
        notificationRepository.save(notification);

        SseEmitter emitter = emitters.get(userId);
        if(emitter != null) {
            log.warn("사용자가 현재 알림을 받을 수 없는 상태입니다: user={}, notificationId={}", userId, notification.getId());

            try {
                log.info("알림 전송: notificationId={}", notification.getId());

                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(NotificationDTO.fromEntity(notification))
                        .reconnectTime(3000));
            } catch (IOException e) {
                emitters.remove(userId);
                emitter.completeWithError(e);
            }
        }
    }
}
