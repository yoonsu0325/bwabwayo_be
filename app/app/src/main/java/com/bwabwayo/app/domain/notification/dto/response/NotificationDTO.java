package com.bwabwayo.app.domain.notification.dto.response;

import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.product.domain.Product;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;

    private String type;

    private String title;

    private LocalDateTime createdAt;

    @Setter
    private String thumbnail;

    public static NotificationDTO fromEntity(Notification notification){
        Product product = notification.getProduct();

        return NotificationDTO.builder()
                .id(notification.getId())
                .title(product.getTitle())
                .thumbnail(product.getThumbnail())
                .type(notification.getType().getTitle())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
