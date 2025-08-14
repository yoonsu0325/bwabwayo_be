package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatMessageRedisEntity;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.chat.dto.MessageSubDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, ChatMessageRedisEntity> messageRedisTemplate;
    private final ChannelTopic messageTopic;
    private final ChannelTopic roomListTopic;


    public RedisPublisher(
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("messageRedisTemplate") RedisTemplate<String, ChatMessageRedisEntity> messageRedisTemplate,
            @Qualifier("messageTopic") ChannelTopic messageTopic,
            @Qualifier("roomListTopic") ChannelTopic roomListTopic
    ) {
        this.redisTemplate = redisTemplate;
        this.messageRedisTemplate = messageRedisTemplate;
        this.messageTopic = messageTopic;
        this.roomListTopic = roomListTopic;
    }


    public void publish(MessageSubDTO message) {
        log.info("RedisPublisher publishing messageSubDTO.. {}", message.getMessageDTO().getContent());
        redisTemplate.convertAndSend(roomListTopic.getTopic(), message);
    }

    public void publish(ChatMessageRedisEntity message) {
        log.info("RedisPublisher publishing messageDTO.. {}", message.getContent());
        messageRedisTemplate.convertAndSend(messageTopic.getTopic(), message);
    }

}
