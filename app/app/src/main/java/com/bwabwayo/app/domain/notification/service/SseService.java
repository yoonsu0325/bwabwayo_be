package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.notification.dto.request.UpsertRequest;
import com.bwabwayo.app.domain.notification.dto.response.NotificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final NotificationService notificationService;

    private final Long TIMEOUT = 1000L * 30;


    // ============= subscribe ==================

    /** 사용자에게 알림을 보낼 수 있도록 등록 */
    public SseEmitter subscribe(String userId, String lastEventId) {
        // 새로운 emitter 연결
        SseEmitter emitter = new SseEmitter(0L);

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

        // 이전 연결 제거
        SseEmitter old = emitters.put(userId, emitter);
        if (old != null) old.complete();

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
        SseEmitter emitter = emitters.get(userId);
        pushEvent(emitter, userId, channel, message);
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
            emitters.remove(userId);
            emitter.completeWithError(e);
            log.error("message={}", e.getMessage(), e);
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
}
