package com.bwabwayo.app.domain.chat.dto;

import com.bwabwayo.app.domain.chat.domain.ChatMessageMongoEntity;
import com.bwabwayo.app.domain.chat.domain.ChatMessageRedisEntity;
import com.bwabwayo.app.domain.chat.domain.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Data
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1l;
    private String content;
    private String senderId;
    private String receiverId;
    private Long roomId;
    @JsonProperty("isRead")
    private boolean read;
    private String createdAt;
    private MessageType type;

    public static MessageDTO fromEntity(ChatMessageMongoEntity chatMessage) {
        return MessageDTO.builder()
                .content(chatMessage.getContent())
                .senderId(chatMessage.getSenderId())
                .receiverId(chatMessage.getReceiverId())
                .roomId(chatMessage.getRoomId())
                .read(chatMessage.getIsRead())
                .createdAt(String.valueOf(chatMessage.getCreatedAt()))
                .type(chatMessage.getType())
                .build();
    }

    public static MessageDTO fromEntity(ChatMessageRedisEntity chatMessage) {
        return MessageDTO.builder()
                .content(chatMessage.getContent())
                .senderId(chatMessage.getSenderId())
                .receiverId(chatMessage.getReceiverId())
                .roomId(chatMessage.getRoomId())
                .read(chatMessage.getIsRead())
                .createdAt(String.valueOf(chatMessage.getCreatedAt()))
                .type(chatMessage.getType())
                .build();
    }
}
