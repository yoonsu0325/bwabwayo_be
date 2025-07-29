package com.bwabwayo.app.domain.chat.controller;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.chat.dto.request.CreateChatRoomRequest;
import com.bwabwayo.app.domain.chat.service.ChatRoomService;
import com.bwabwayo.app.domain.chat.service.ChatService;
import com.bwabwayo.app.domain.chat.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RedisController {

    private final RedisService redisService;
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    @MessageMapping("/chat/message")
    public void message(MessageDTO messageDTO){
        log.info("💬 메시지 수신: {}", messageDTO);
        redisService.save(messageDTO);
        chatService.sendChatMessage(messageDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody CreateChatRoomRequest request) {
        ChatRoom chatRoom = chatRoomService.createRoom(request);
        return ResponseEntity.ok(chatRoom);
    }

/*    @PostMapping("/send")
    public void sendMessage(@RequestParam String channel, @RequestBody MessageDTO messageDTO){
        log.info("Redis Pub MSG Channel = {}", channel);
        redisService.pubMsgChannel(channel, messageDTO);
    }

    @PostMapping("/cancel")
    public void cancelSubChannel(@RequestParam String channel){
        redisService.cancelSubChannel(channel);
    }*/
}
