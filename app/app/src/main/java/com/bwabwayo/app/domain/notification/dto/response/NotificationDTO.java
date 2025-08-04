package com.bwabwayo.app.domain.notification.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private String type;

    private String title;

    private LocalDateTime createdAt;

    @Setter
    private String thumbnail;
}
