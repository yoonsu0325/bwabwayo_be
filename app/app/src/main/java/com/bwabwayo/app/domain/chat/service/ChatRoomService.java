package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.dto.request.CreateChatRoomRequest;
import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRedisRepository;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomRedisRepository chatRoomRedisRepository;

    private static final String CHAT_ROOMS_KEY = "CHAT_ROOMS";

    /**
     * 채팅방 생성: MySQL 저장 + Redis 캐싱
     */
    @Transactional
    public ChatRoom createRoom(CreateChatRoomRequest request) {
        // 1. DB 저장
        ChatRoom chatRoom = ChatRoom.createRoom(request);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        Long roomId = savedChatRoom.getRoomId();
        Long buyerId = savedChatRoom.getBuyerId();
        Long sellerId = savedChatRoom.getSellerId();

        // 2. Redis 캐싱용 기본 미리보기 응답 생성
        ChatRoomListResponse buyerPreview = ChatRoomListResponse.fromInitial(savedChatRoom, sellerId);
        ChatRoomListResponse sellerPreview = ChatRoomListResponse.fromInitial(savedChatRoom, buyerId);

        // 3. Redis에 캐싱
        chatRoomRedisRepository.setChatRoom(buyerId, roomId, buyerPreview);
        chatRoomRedisRepository.setChatRoom(sellerId, roomId, sellerPreview);

        return savedChatRoom;
    }

    /**
     * 채팅방 목록 조회
     */
    public List<ChatRoomListResponse> getChatRoomList(Long userId) {
        List<ChatRoomListResponse> roomList;

        // Redis에 존재하면 가져오기
        if (chatRoomRedisRepository.existChatRoomList(userId)) {
            roomList = chatRoomRedisRepository.getChatRoomList(userId);
        } else {
            // Redis에 없으면 DB에서 조회 (예: join fetch 필요 시)
            List<ChatRoom> chatRooms = chatRoomRepository.findBySellerIdOrBuyerId(userId, userId);
            roomList = new ArrayList<>();

            for (ChatRoom chatRoom : chatRooms) {
                Long partnerId = chatRoom.getOtherUserId(userId); // 상대 유저 ID 구하는 메서드 필요
                roomList.add(ChatRoomListResponse.fromInitial(chatRoom, partnerId));
            }

            chatRoomRedisRepository.initChatRoomList(userId, roomList); // 캐싱
        }

        return sortChatRoomListLatest(roomList);
    }

    /**
     * 마지막 메시지 기준 정렬
     */
    public List<ChatRoomListResponse> sortChatRoomListLatest(List<ChatRoomListResponse> list) {
        List<ChatRoomListResponse> filtered = new ArrayList<>();
        for (ChatRoomListResponse response : list) {
            if (response.getLastChatmessageDto() != null) {
                filtered.add(response);
            }
        }

        filtered.sort((o1, o2) ->
                o2.getLastChatmessageDto().getCreatedAt().compareTo(o1.getLastChatmessageDto().getCreatedAt()));
        return filtered;
    }

    /**
     * Redis에서 채팅 메시지용 room 초기화 (선택 사항)
     */
    public void genNewRoom(Long roomId) {
        // 예: 채팅 메시지 저장용 키 초기화 등
        // redisTemplate.opsForList().leftPush("chat:" + roomId, "");
    }

    public ChatRoomListResponse getChatRoomInfo(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다: roomId=" + roomId));

        // sellerId와 buyerId를 바탕으로 상대방 정보 세팅 (예: 닉네임, 프로필 이미지 등)
        // 일단 더미데이터
        String productName = "아이폰 14 S급 팔아요";
        Integer productPrice = 500000;
        String sellerProfileImageUrl = "sellerProfileImageUrl";
        String buyerProfileImageUrl = "buyerProfileImageUrl";
        String productImageUrl = "productImageUrl";
        String myNickName = "Tommy";
        String parterNickName = "Grace";

        return ChatRoomListResponse.fromInitial(chatRoom, userId, productName, productPrice, sellerProfileImageUrl, buyerProfileImageUrl
        , productImageUrl, myNickName, parterNickName);
    }

}
