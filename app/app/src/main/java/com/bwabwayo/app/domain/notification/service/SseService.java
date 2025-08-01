package com.bwabwayo.app.domain.notification.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    @Value("${sse.timeout}")
    private Long timeout;

    /**
     * 사용자에게 알림을 보낼 수 있도록 등록
     */
    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(timeout);
        
        // 이전 연결 제거
        SseEmitter old = emitters.put(userId, emitter);
        if(old != null) old.complete();

        // 연결종료, 타임아웃, 에러발생 시 emitter 제거 (메모리 누수 방지)
        log.debug("SSE 구독 시작: userId={}", userId);
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.debug("SSE 연결 종료: userId={}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.debug("SSE 타임 아웃: userId={}", userId);
        });
        emitter.onError((e) -> {
            emitters.remove(userId);
            log.debug("SSE 에러 발생: userId={}", userId);
        });

        try {
            // 연결 확인용 event 발송
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected")
                    .reconnectTime(3000));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
    
    public void send(String userId, String message) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(message)
                        .reconnectTime(3000));
            } catch (IOException e) {
                emitters.remove(userId);
                emitter.completeWithError(e);
            }
        }
    }
}
