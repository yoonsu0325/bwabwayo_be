package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatMessageRedisEntity;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RedisService {

    private final RedisTemplate<String, ChatMessageRedisEntity> redisTemplate;
    private final ChatMongoService chatMongoService;

    public RedisService(
            @Qualifier("messageRedisTemplate")
            RedisTemplate<String, ChatMessageRedisEntity> redisTemplate, ChatMongoService chatMongoService
    ) {
        this.redisTemplate = redisTemplate;
        this.chatMongoService = chatMongoService;
    }
    private static final int PAGE_SIZE = 20;
    private static final int MAX_CACHE_SIZE = 30;
    public List<MessageDTO> findMessages(Long roomId, int pageNumber) {
        String key = "chat:room:" + roomId;
        long start = (long) pageNumber * PAGE_SIZE;
        long end = start + PAGE_SIZE - 1;
        log.info(key);
        List<ChatMessageRedisEntity> cachedMessages = redisTemplate.opsForList().range(key, start, end);

        if (cachedMessages.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(cachedMessages, Comparator.comparing(ChatMessageRedisEntity::getCreatedAt));
        return cachedMessages.stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());

    }



    public ChatMessageRedisEntity saveMessageToRedis(MessageDTO message) {
        String redisKey = "chat:room:" + message.getRoomId();
        ChatMessageRedisEntity entity = ChatMessageRedisEntity.of(message);
        redisTemplate.opsForList().leftPush(redisKey, entity);
        redisTemplate.opsForList().trim(redisKey, 0, 99);

        // 캐시 사이즈 초과 시 -> MongoDB로 이전
        Long size = redisTemplate.opsForList().size(redisKey);
        if (size != null && size >= MAX_CACHE_SIZE) {
            List<ChatMessageRedisEntity> messages = redisTemplate.opsForList().range(redisKey, 0, -1);

            if (messages != null) {
                for (ChatMessageRedisEntity chatMessageRedisEntity : messages) {
                    chatMongoService.save(MessageDTO.fromEntity(chatMessageRedisEntity));
                }
                redisTemplate.delete(redisKey); // 캐시 초기화
            }
        }
        return entity;
    }

    public void markAsRead(ChatMessageRedisEntity message) {
        String key = "chat:room:" + message.getRoomId();

        List<ChatMessageRedisEntity> list = redisTemplate.opsForList().range(key, 0, -1);
        if (list == null || list.isEmpty()) return;

        for (int i = 0; i < list.size(); i++) {
            ChatMessageRedisEntity m = list.get(i);
            if (Objects.equals(m.getCreatedAt(), message.getCreatedAt())
                    && Objects.equals(m.getSenderId(), message.getSenderId())
                    && Objects.equals(m.getContent(), message.getContent())) {

                // ✅ 읽음 처리
                m.setIsRead(true);
                redisTemplate.opsForList().set(key, i, m);
                break;
            }
        }
    }


    public Optional<ChatMessageRedisEntity> findLastMessage(Long roomId) {
        String key = "chat:room:" + roomId;
        List<ChatMessageRedisEntity> lastMsgList = redisTemplate.opsForList().range(key, 0 , 0);
        log.info("findLast" + String.valueOf(lastMsgList.get(0).getContent()));
        if (lastMsgList != null && !lastMsgList.isEmpty()) {
            return Optional.of(lastMsgList.get(0));
        }
        return Optional.empty();
    }

    public long countUnreadMessages(Long roomId, String userId) {
        String key = "chat:room:" + roomId;
        List<ChatMessageRedisEntity> messages = redisTemplate.opsForList().range(key, 0, -1);
        return messages.stream()
                .filter(msg -> !msg.getSenderId().equals(userId) && !Boolean.TRUE.equals(msg.getIsRead()))
                .count();
    }

}
