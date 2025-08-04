package com.bwabwayo.app.domain.chat.controller;

import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RedisController {

    private final ChatService chatService;
    @MessageMapping("/chat/message")
    public void message(MessageDTO messageDTO){
        log.info("💬 메시지 수신: {}", messageDTO);
        chatService.sendChatMessage(messageDTO);
    }
}
