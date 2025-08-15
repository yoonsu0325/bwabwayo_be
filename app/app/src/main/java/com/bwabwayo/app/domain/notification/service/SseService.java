package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.notification.dto.request.UpsertRequest;
import com.bwabwayo.app.domain.notification.dto.response.NotificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.internal.function.CheckedConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final Map<String, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final NotificationService notificationService;

    private final Long TIMEOUT = 0L;


    // ============= subscribe ==================

    /** 사용자에게 알림을 보낼 수 있도록 등록 */
    public SseEmitter subscribe(String userId, String lastEventId) {
        // 새로운 emitter 연결
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        Set<SseEmitter> userEmitters = emitters.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        userEmitters.add(emitter);

        Consumer<String> cleanup = (String message) -> {
            Set<SseEmitter> set = emitters.get(userId);
            if (set != null) {
                set.remove(emitter);
                if (set.isEmpty()) {
                    emitters.remove(userId);
                }
            }
            log.info(message + ": userId={}", userId);
        };

        log.info("SSE 구독 시작: userId={}", userId);
        emitter.onCompletion(()->cleanup.accept("SSE 연결 종료"));
        emitter.onTimeout(()->cleanup.accept("SSE 타임 아웃"));
        emitter.onTimeout(()->cleanup.accept("SSE 에러 발생"));

        // 연결 확인용 event 발송
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(System.currentTimeMillis()))
                    .name("connect")
                    .data("connected")
                    .reconnectTime(3000));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        pushEvent(userId);

        return emitter;
    }

    // ============= push-event ==================

    /** 기본 메시지 전송 */
    public void pushEvent(String userId){
        pushEvent(userId, "notification","알림이 도착했습니다.");
    }
    /** 메시지 전송 */
    public void pushEvent(String userId, String channel, String message){
        if(!emitters.containsKey(userId)) return;
        for(SseEmitter emitter : emitters.get(userId)){
            pushEvent(emitter, userId, channel, message);
        }
    }

    private void pushEvent(SseEmitter emitter, String userId, String channel, String message){
        if(emitter == null) return;

        try {
            log.info("알림 전송: userId={}, message={}", userId, message);

            emitter.send(SseEmitter.event()
                    .name(channel)
                    .data(message)
                    .reconnectTime(3000L));
        } catch (IOException e) {
            // 1) 먼저 조용히 종료 (onCompletion 훅이 있다면 그쪽에서 제거 가능)
            try { emitter.complete(); } catch (Throwable ignore) {}

            // 2) 원자적으로 emitter 제거
            emitters.computeIfPresent(userId, (k, set) -> {
                set.remove(emitter);
                return set.isEmpty() ? null : set;
            });

            // 3) 로그 출력
            log.error("SSE send failed: userId={}, channel={}, message={}", userId, channel, e.getMessage(), e);
        }
    }

    // ============= upsert-notification ==================

    public void upsertNotification(UpsertRequest request){
        String receiverId = request.getReceiverId();
        Long productId = request.getProductId();
        Long chatroomId = request.getChatroomId();
        String message = request.getMessage();

        notificationService.upsert(receiverId, productId, chatroomId, message);

        pushEvent(receiverId);
    }

    public void upsertHint(UpsertRequest request){
        String receiverId = request.getReceiverId();
        Long productId = request.getProductId();
        Long chatroomId = request.getChatroomId();
        String message = request.getMessage();

        NotificationResponse build = NotificationResponse
                .builder()
                .receiverId(receiverId)
                .productId(productId)
                .chatroomId(chatroomId)
                .message(message)
                .build();
        try{
            String json = new ObjectMapper().writeValueAsString(build);
            log.info("json={}", json);
            pushEvent(receiverId, "hint", json);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void handleMessage(MessageDTO message){
        log.info("알림 대상: {}", message.toString());
//        if(true) return;

        String contnet = message.getContent();
        switch (message.getType()){
            case TEXT: {
                upsertHint(UpsertRequest.of(message.getReceiverId(), null, message.getRoomId(), contnet));
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
        upsertHint(UpsertRequest.of(message.getReceiverId(), null, message.getRoomId(), contnet));
        upsertHint(UpsertRequest.of(message.getReceiverId(), null, message.getRoomId(), contnet));
    }


    @Scheduled(fixedRate = 15000, initialDelay = 5_000) // 15초
    public void sendKeepAlive() {
        for (Map.Entry<String, Set<SseEmitter>> entry : emitters.entrySet()) {
            String userId = entry.getKey();
            Set<SseEmitter> set = entry.getValue();

            // 스냅샷으로 순회(동시 제거 안전)
            for (SseEmitter e : List.copyOf(set)) {
                try {
                    // 같은 emitter로의 동시 send 보호(간단 동기화)
                    synchronized (e) {
                        e.send(SseEmitter.event()
                                .name("ping")
                                .data("keepalive")  // comment만 보내지 말고 data 포함!
                                .reconnectTime(3000));
                    }
                } catch (IllegalStateException | IOException ex) {
                    // 전송 실패 -> 정리
                    try { e.complete(); } catch (Exception ignore) {}
                    set.remove(e);
                    if (set.isEmpty()) {
                        emitters.remove(userId);
                    }
                }
            }
        }
    }
}
