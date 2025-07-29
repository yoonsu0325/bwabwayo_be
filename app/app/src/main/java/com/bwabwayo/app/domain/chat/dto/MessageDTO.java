package com.bwabwayo.app.domain.chat.dto;

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
    private Long senderId;
    private Long receiverId;
    private Long roomId;
    @JsonProperty("isRead")
    private boolean read;
    private String createdAt;
    private MessageType type;
}
