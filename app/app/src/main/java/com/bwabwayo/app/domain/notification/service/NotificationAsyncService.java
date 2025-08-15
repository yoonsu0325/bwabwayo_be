package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAsyncService {

    private final SseService sseService; // 실제 handleMessage 보유

    @Async("notificationExecutor") // ThreadPoolTaskExecutor bean 이름
    public void handleMessageAsync(MessageDTO message) {
        try {
            sseService.handleMessage(message);
        } catch (Exception e) {
            // 예외를 삼켜서 채팅 플로우에 영향 주지 않게
            log.error("handleMessage 처리 중 예외", e);
        }
    }
}