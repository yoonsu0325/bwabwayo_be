package com.bwabwayo.app.domain.notification.dto;

import com.bwabwayo.app.domain.notification.domain.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;

    private String receiverId;

    private Long productId;   // null 가능

    private Long chatroomId;  // null 가능

    private String message;

    private LocalDateTime updatedAt; // Asia/Seoul

    private boolean isRead;

    private int unreadCount;

    public static NotificationDTO from(Notification notification){
        return NotificationDTO.builder()
                .id(notification.getId())
                .receiverId(notification.getReceiver().getId())
                .productId(notification.getProduct() == null ? null : notification.getProduct().getId())
                .chatroomId(notification.getChatRoom() == null ? null : notification.getChatRoom().getRoomId())
                .message(notification.getMessage())
                .updatedAt(notification.getUpdatedAt())
                .isRead(notification.isRead())
                .unreadCount(notification.getUnreadCount())
                .build();
    }
}
