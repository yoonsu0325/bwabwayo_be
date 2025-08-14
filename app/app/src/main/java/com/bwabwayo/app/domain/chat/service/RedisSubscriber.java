package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.chat.dto.MessageSubDTO;
import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
import com.bwabwayo.app.domain.notification.dto.request.UpsertRequest;
import com.bwabwayo.app.domain.notification.service.NotificationService;
import com.bwabwayo.app.domain.notification.service.SseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisSubscriber {
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;
    private final SseService sseService;

    public void sendMessage(String publishMessage) {
        try {
            System.out.println("✅ Redis 메시지 수신됨! 메시지: " +  publishMessage);
            MessageDTO chatMessage = objectMapper.readValue(publishMessage, MessageDTO.class);

            messagingTemplate.convertAndSend(
                    "/sub/chat/room/" + chatMessage.getRoomId(), chatMessage
            );

            sseService.handleMessage(chatMessage);
        } catch (Exception e) {
            log.error("[sendMessage] Exception: {}", e.getMessage(), e);
        }
    }

    public void sendRoomList(String publishMessage) {
        try {
            System.out.println("✅ Redis 메시지 수신됨! 메시지: " +  publishMessage);
            MessageSubDTO dto = objectMapper.readValue(publishMessage, MessageSubDTO.class);

            List<ChatRoomListResponse> chatRoomListGetResponseList = dto.getList();
            List<ChatRoomListResponse> chatRoomListGetResponseListPartner = dto.getPartnerList();

            String userId = dto.getUserId();
            String partnerId = dto.getPartnerId();


            // 로그인 유저 채팅방 리스트 최신화 -> 내 계정에 보냄
            messagingTemplate.convertAndSendToUser(
                    userId, // JwtChannelInterceptor에서 acc.setUser(name=userId)
                    "/sub/chat/roomlist",
                    chatRoomListGetResponseList
            );
            /*messagingTemplate.convertAndSend(
                    "/sub/chat/roomlist/" + userId, chatRoomListGetResponseList
            );*/

            // 파트너 계정에도 리스트 최신화 보냄.
            messagingTemplate.convertAndSendToUser(
                    partnerId,
                    "/sub/chat/roomlist",
                    chatRoomListGetResponseListPartner
            );

            /*messagingTemplate.convertAndSend(
                    "/sub/chat/roomlist/" + partnerId, chatRoomListGetResponseListPartner
            );*/

        } catch (Exception e) {
            log.error("Exception {}", e);
        }
    }

}
