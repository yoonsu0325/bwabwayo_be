package com.bwabwayo.app.domain.chat.repository;

import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomRedisRepository {

    private static final String CHAT_ROOM_KEY = "_CHAT_ROOM_RESPONSE_LIST";
    @Autowired
    private RedisTemplate<String, ChatRoomListResponse> chatRoomRedisTemplate;

    private final ObjectMapper objectMapper;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoomListResponse> opsHashChatRoom;

    private String getChatRoomKey(Long userId) {
        return userId + CHAT_ROOM_KEY;
    }

    public boolean existChatRoomList(Long userId) {
        return chatRoomRedisTemplate.hasKey(getChatRoomKey(userId));
    }

    public void initChatRoomList(Long userId, List<ChatRoomListResponse> list) {
        if (chatRoomRedisTemplate.hasKey(getChatRoomKey(userId))) {
            chatRoomRedisTemplate.delete(getChatRoomKey(userId));
        }

        opsHashChatRoom = chatRoomRedisTemplate.opsForHash();
        for (ChatRoomListResponse chatRoomListGetRes : list) {
            setChatRoom(userId, chatRoomListGetRes.getRoomId(), chatRoomListGetRes);
        }
    }

    public void setChatRoom(Long userId, Long roomId, ChatRoomListResponse chatRoomListResponse) {
        opsHashChatRoom.put(getChatRoomKey(userId), String.valueOf(roomId), chatRoomListResponse);
    }

    public List<ChatRoomListResponse> getChatRoomList(Long userId) {
        // 채팅방 리스트 조회
        return objectMapper.convertValue(opsHashChatRoom.values(getChatRoomKey(userId)), new TypeReference<>() {});
    }

    public boolean existChatRoom(Long userId, Long roomId) {
        return opsHashChatRoom.hasKey(getChatRoomKey(userId), String.valueOf(roomId));
    }

    public ChatRoomListResponse getChatRoom(Long userId, Long roomId) {
        String redisKey = userId + "_CHAT_ROOM_RESPONSE_LIST";
        String fieldKey = String.valueOf(roomId);

        Object raw = chatRoomRedisTemplate.opsForHash().get(redisKey, fieldKey);

        if (raw instanceof ChatRoomListResponse response) {
            return response; // ✅ 직렬화 성공
        }

        throw new IllegalStateException("❌ Redis 역직렬화 실패: " + raw.getClass());
    }

}