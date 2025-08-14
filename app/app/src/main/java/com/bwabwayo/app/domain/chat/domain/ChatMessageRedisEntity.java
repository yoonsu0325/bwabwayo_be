package com.bwabwayo.app.domain.chat.domain;

import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRedisEntity{
    private String content;
    private String senderId;
    private String receiverId;
    private Long roomId;
    private Boolean isRead;
    private String createdAt;
    private MessageType type;

    public static ChatMessageRedisEntity of(MessageDTO dto) {
        return ChatMessageRedisEntity.builder()
                .senderId(dto.getSenderId())
                .receiverId(dto.getReceiverId())
                .roomId(dto.getRoomId())
                .content(dto.getContent())
                .createdAt(dto.getCreatedAt())
                .type(dto.getType())
                .isRead(false)
                .build();
    }
}
