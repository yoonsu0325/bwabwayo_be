package com.bwabwayo.app.domain.chat.controller;

import com.bwabwayo.app.domain.chat.domain.ChatMessageRedisEntity;
import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.domain.MessageType;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.chat.dto.request.CreateChatRoomRequest;
import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
import com.bwabwayo.app.domain.chat.service.ChatMongoService;
import com.bwabwayo.app.domain.chat.service.ChatRoomService;
import com.bwabwayo.app.domain.chat.service.ChatService;
import com.bwabwayo.app.domain.chat.service.RedisService;
import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.notification.service.NotificationService;
import com.bwabwayo.app.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMongoService chatMongoService;
    private final RedisService redisService;
    private final ChatService chatService;
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<ChatRoomListResponse>> getChatRoomList(
            @LoginUser User user){

        return ResponseEntity.ok(chatRoomService.getChatRoomList(user.getId()));
    }


    @GetMapping("/{roomId}")
    public ResponseEntity<?> roomFindInfo(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "page") Integer pageNumber,
            @LoginUser User user
    ) {
        List<MessageDTO> messages = redisService.findMessages(roomId, pageNumber);

        // Redis에 데이터가 없으면 MongoDB에서 조회
        if (messages.isEmpty()) {
            messages = chatMongoService.findAll(roomId, pageNumber);
        }

        messages.stream()
                .filter(msg -> !msg.getSenderId().equals(user.getId()) && !msg.isRead())
                .forEach(msg -> {
                    ChatMessageRedisEntity entity = ChatMessageRedisEntity.of(msg); // DTO → Entity 변환 메서드 필요
                    redisService.markAsRead(entity);
                });
//        notificationService.markChatRead(user.getId(), roomId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<ChatRoom> createChatRoom(
            @LoginUser User user,
            @RequestBody CreateChatRoomRequest request) {

        Optional<ChatRoom> optChatRoom = chatRoomService.find(request, user);
        ChatRoom chatRoom = null;
        if(optChatRoom.isPresent()){
            chatRoom = optChatRoom.get();
        }
        else{
            chatRoom = chatRoomService.createRoom(request, user);

            MessageDTO messageDTO = MessageDTO.builder()
                    .content("")
                    .senderId(user.getId())
                    .receiverId(request.getSellerId())
                    .roomId(chatRoom.getRoomId())
                    .read(false)
                    .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                    .type(MessageType.CREATE_ROOM).build();
            chatService.sendChatMessage(messageDTO);
        }

        return ResponseEntity.ok(chatRoom);
    }
}
