package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.notification.dto.request.UpsertRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final NotificationService notificationService;
    @Value("${sse.timeout}")
    private Long timeout;

    /**
     * 사용자에게 알림을 보낼 수 있도록 등록
     */
    public SseEmitter subscribe(String userId, String lastEventId) {
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

        pushEvent(userId);

        return emitter;
    }

    public void pushEvent(String userId){
        SseEmitter emitter = emitters.get(userId);
        pushEvent(emitter, userId);
    }
    private void pushEvent(SseEmitter emitter, String userId){
        if(emitter == null) return;

        try {
            log.info("알림 전송");

            emitter.send(SseEmitter.event()
//                    .id(String.valueOf(toEpochMilli(notification.getUpdatedAt())))
                    .name("notification")
                    .data("새로운 알림을 조회하세요.")
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

    public void upsertChatNotification(Long chatId, UpsertRequest request){
        String receiverId = request.getReceiverId();
        String message = request.getMessage();

        notificationService.upsertChat(receiverId, chatId, message, 1);

        pushEvent(receiverId);
    }

    public void upsertProductNotification(Long productId, UpsertRequest request){
        String receiverId = request.getReceiverId();
        String message = request.getMessage();

        notificationService.upsertProduct(receiverId, productId, message);

        pushEvent(receiverId);
    }

    public void handleMessage(MessageDTO message){
        if(true) return;

        String contnet = message.getContent();
        switch (message.getType()){
            case TEXT: {
                upsertChatNotification(message.getRoomId(), UpsertRequest.of(message.getReceiverId(), contnet));
                return;
            }
            case IMAGE: contnet = "이미지 파일입니다."; break;
            case CREATE_ROOM: contnet = "채팅방이 생성되었습니다."; break;
            case RESERVE_VIDEOCALL: contnet = "화상 거래가 예약되었습니다"; break;
            case CANCEL_VIDEOCALL: contnet = "화상 거래가 취소되었습니다"; break;
            case START_VIDEOCALL: contnet = "화상 거래가 시작되었습니다"; break;
            case START_TRADE: contnet = "거래가 시작되었습니다"; break;
            case REQUEST_DEPOSIT: contnet = "입금하세요"; break;
            case INPUT_DELIVERY_ADDRESS: contnet = "배송지를 입력하세요"; break;
            case INPUT_TRACKING_NUMBER: contnet = "송장번호를 입력하세요"; break;
            case START_DELIVERY: contnet = "배송이 시작되었습니다."; break;
            case CONFIRM_PURCHASE: contnet = "구매가 확정되었습니다."; break;
            case END_TRADE: contnet = "거래가 종료됩니다."; break;
        }
        upsertChatNotification(message.getRoomId(), UpsertRequest.of(message.getReceiverId(), contnet));
        upsertChatNotification(message.getRoomId(), UpsertRequest.of(message.getSenderId(), contnet));
    }
}
