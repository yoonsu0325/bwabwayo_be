package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.chat.dto.MessageSubDTO;
import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {
    private final RedisPublisher redisPublisher;
    private final ChatRoomRedisRepository chatRoomRedisRepository;
    private final ChatRoomService chatRoomService;

    public void sendChatMessage(MessageDTO chatMessage) {
        log.info("📢 메시지 브로드캐스트: {}", chatMessage);
        Long userId = chatMessage.getSenderId();
        Long partnerId;

        ChatRoomListResponse newChatRoomList = null;

        if (chatRoomRedisRepository.existChatRoom(userId, chatMessage.getRoomId())) {
            newChatRoomList = chatRoomRedisRepository.getChatRoom(userId, chatMessage.getRoomId());
        } else {
            newChatRoomList = chatRoomService.getChatRoomInfo(chatMessage.getRoomId(), userId);
        }

        partnerId = getPartnerId(chatMessage, newChatRoomList);

        // 2. 채팅방 리스트에 새로운 채팅방 정보가 없다면, 넣어준다. 상대방 레디스에도 업데이트 해준다.
        setNewChatRoomInfo(chatMessage, newChatRoomList);

        // 3. 마지막 메시지들이 담긴 채팅방 리스트들을 가져온다.
        List<ChatRoomListResponse> chatRoomListGetResponseList = chatRoomService.getChatRoomList(userId);
        // 4. 파트너 채팅방 리스트도 가져온다. (파트너는 userId 로만)
        List<ChatRoomListResponse> partnerChatRoomGetResponseList = chatRoomService.getChatRoomList(partnerId);

        // 5. 마지막 메세지 기준으로 정렬 채팅방 리스트 정렬
        chatRoomListGetResponseList = chatRoomService.sortChatRoomListLatest(chatRoomListGetResponseList);
        partnerChatRoomGetResponseList = chatRoomService.sortChatRoomListLatest(partnerChatRoomGetResponseList);

        MessageSubDTO messageSubDto = MessageSubDTO.builder()
                .userId(userId)
                .partnerId(partnerId)
                .messageDTO(chatMessage)
                .list(chatRoomListGetResponseList)
                .partnerList(partnerChatRoomGetResponseList)
                .build();

        redisPublisher.publish(messageSubDto);
    }

/*    private void setNewChatRoomInfo(MessageDTO chatMessage, ChatRoomListResponse newChatRoomList) {
        Long roomId = chatMessage.getRoomId();
        Long senderId = chatMessage.getSenderId();
        Long receiverId = newChatRoomList.getPartnerId(); // 상대방 ID

        // ✅ 내 채팅방 정보 갱신
        ChatRoomListResponse myChatRoomPreview = chatRoomRedisRepository.getChatRoom(senderId, roomId);
        if (myChatRoomPreview != null) {
            myChatRoomPreview.setLastMessageTime(chatMessage.getCreatedAt());
            myChatRoomPreview.setUnreadMessagesNum(0); // 내가 보낸 메시지니까 안읽은 메시지 없음
            chatRoomRedisRepository.setChatRoom(senderId, roomId, myChatRoomPreview);
        }

        // ✅ 상대방 채팅방 정보 갱신
        ChatRoomListResponse partnerChatRoomPreview = chatRoomRedisRepository.getChatRoom(receiverId, roomId);
        if (partnerChatRoomPreview != null) {
            // Redis에 있으면 안읽은 메시지 +1
            int currentUnread = partnerChatRoomPreview.getUnreadMessagesNum();
            partnerChatRoomPreview.setUnreadMessagesNum(currentUnread + 1);
            partnerChatRoomPreview.setLastMessageTime(chatMessage.getCreatedAt());
            chatRoomRedisRepository.setChatRoom(receiverId, roomId, partnerChatRoomPreview);
        } else {
            // Redis에 없으면 DB에서 채팅방 정보 가져와서 만들고 set
            ChatRoom chatRoom = chatRoomService.getChatRoomFromDB(roomId);
            ChatRoomListResponse createdPreview = ChatRoomListResponse.from(chatRoom, chatMessage);
            createdPreview.setUnreadMessagesNum(1); // 처음 메시지니까 1
            chatRoomRedisRepository.setChatRoom(receiverId, roomId, createdPreview);
        }
    }*/

    private void setNewChatRoomInfo(MessageDTO chatMessage, ChatRoomListResponse newChatRoomListResponse) {

        newChatRoomListResponse.updateChatMessageDto(chatMessage);

        /** 상대방 채팅 리스트와 내 리스트 둘다 채팅방을 저장한다. */

        if (newChatRoomListResponse.getUserId().equals(newChatRoomListResponse.getSellerId())) {
            chatRoomRedisRepository.setChatRoom(newChatRoomListResponse.getSellerId(),
                    chatMessage.getRoomId(), newChatRoomListResponse);

            newChatRoomListResponse.changePartnerInfo(); //닉네임 체인지
            chatRoomRedisRepository.setChatRoom(newChatRoomListResponse.getBuyerId(), chatMessage.getRoomId(), newChatRoomListResponse);

        } else if (newChatRoomListResponse.getUserId().equals(newChatRoomListResponse.getBuyerId())){
            chatRoomRedisRepository.setChatRoom(newChatRoomListResponse.getBuyerId(),
                    chatMessage.getRoomId(), newChatRoomListResponse);

            newChatRoomListResponse.changePartnerInfo(); //닉네임 체인지
            chatRoomRedisRepository.setChatRoom(newChatRoomListResponse.getSellerId(), chatMessage.getRoomId(), newChatRoomListResponse);
        }
        //다시 원상태로 복귀
        newChatRoomListResponse.changePartnerInfo();

    }

    // redis에서 채팅방 리스트 불러오는 로직
    private List<ChatRoomListResponse> getChatRoomListByUserId(Long userId) {
        List<ChatRoomListResponse> chatRoomListGetResponseList = new ArrayList<>();

        if (chatRoomRedisRepository.existChatRoomList(userId)) {
            chatRoomListGetResponseList = chatRoomRedisRepository.getChatRoomList(userId);
/*            for (ChatRoomListResponse chatRoomListGetResponse : chatRoomListGetResponseList) {
                chatRoomService.setListChatLastMessage(chatRoomListGetResponse);
            }*/
        }


        return chatRoomListGetResponseList;
    }

    private Long getPartnerId(MessageDTO chatMessageDto, ChatRoomListResponse my) {
        Long userId = chatMessageDto.getSenderId();
        Long partnerId;
        if (my.getBuyerId() == userId) {
            partnerId = my.getSellerId();
        } else {
            partnerId = my.getBuyerId();
        }
        return partnerId;
    }
}
