package com.bwabwayo.app.domain.chat.domain;

import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessageMongoEntity {
    private String content;
    private String senderId;
    private String receiverId;
    private Long roomId;
    private Boolean isRead;
    @Field("createdAt")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAt;
    private MessageType type;

    public static ChatMessageMongoEntity of(MessageDTO dto) {
        return ChatMessageMongoEntity.builder()
                .senderId(dto.getSenderId())
                .receiverId(dto.getReceiverId())
                .roomId(dto.getRoomId())
                .content(dto.getContent())
                .createdAt(LocalDateTime.parse(dto.getCreatedAt()))
                .type(dto.getType())
                .isRead(dto.isRead())
                .build();
    }
}
