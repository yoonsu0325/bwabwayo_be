package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatMessageRedisEntity;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    public void save(MessageDTO messageDTO) {
        String redisKey = "ChatMessages:" + messageDTO.getRoomId();
        ChatMessageRedisEntity entity = ChatMessageRedisEntity.of(messageDTO);

        // 객체 그대로 저장
        redisTemplate.opsForList().leftPush(redisKey, entity);
        redisTemplate.opsForList().trim(redisKey, 0, 99);
        log.info("✅ 저장된 객체: {}", entity);
    }


}
