package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.chat.dto.MessageSubDTO;
import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
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

    public void sendMessage(String publishMessage) {
        try {

            MessageDTO chatMessage =
                    objectMapper.readValue(publishMessage, MessageSubDTO.class).getMessageDTO();

            // 채팅방을 구독한 클라이언트에게 메시지 발송
            messagingTemplate.convertAndSend(
                    "/sub/chat/room/" + chatMessage.getRoomId(), chatMessage
            );

        } catch (Exception e) {
            log.error("Exception {}", e);
        }
    }

    public void sendRoomList(String publishMessage) {
        try {
            MessageSubDTO dto = objectMapper.readValue(publishMessage, MessageSubDTO.class);

            List<ChatRoomListResponse> chatRoomListGetResponseList = dto.getList();
            List<ChatRoomListResponse> chatRoomListGetResponseListPartner = dto.getPartnerList();

            Long userId = dto.getUserId();
            Long partnerId = dto.getPartnerId();

            // 로그인 유저 채팅방 리스트 최신화 -> 내 계정에 보냄
            messagingTemplate.convertAndSend(
                    "/sub/chat/roomlist/" + userId, chatRoomListGetResponseList
            );

            // 파트너 계정에도 리스트 최신화 보냄.
            messagingTemplate.convertAndSend(
                    "/sub/chat/roomlist/" + partnerId, chatRoomListGetResponseListPartner
            );

        } catch (Exception e) {
            log.error("Exception {}", e);
        }
    }

}
