package com.bwabwayo.app.domain.notification.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long id;

    private String title; // 대상 상품
    
    private String message; // 메시지 내용
    
    private LocalDateTime createdAt; // 발신 시각

    private String thumbnail; // 상품의 썸네일

    private Integer unreadCount;
}
