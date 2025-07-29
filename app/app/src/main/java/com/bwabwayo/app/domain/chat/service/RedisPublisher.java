package com.bwabwayo.app.domain.chat.service;

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
    private final ChannelTopic messageTopic;
    private final ChannelTopic roomListTopic;


    public RedisPublisher(
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("messageTopic") ChannelTopic messageTopic,
            @Qualifier("roomListTopic") ChannelTopic roomListTopic
    ) {
        this.redisTemplate = redisTemplate;
        this.messageTopic = messageTopic;
        this.roomListTopic = roomListTopic;
    }


    public void publish(MessageSubDTO message) {
        log.info("RedisPublisher publishing messageSubDTO.. {}", message.getMessageDTO().getContent());
        redisTemplate.convertAndSend(roomListTopic.getTopic(), message);
    }

    public void publish(MessageDTO message) {
        log.info("RedisPublisher publishing messageDTO.. {}", message.getContent());
        redisTemplate.convertAndSend(messageTopic.getTopic(), message);
    }

}
