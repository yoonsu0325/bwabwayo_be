package com.bwabwayo.app.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class NotificationResponse {

    private Long id;

    private String title; // 대상 상품
    
    private String message; // 메시지 내용
    
    private LocalDateTime createdAt; // 발신 시각

    private String thumbnail; // 썸네일

    private Integer unreadCount; // 읽지 않은 메시지의 수

    private String receiverId; // 수신자 ID

    private Long productId; // 상품 ID

    private Long chatroomId; // 채팅방 ID
}
