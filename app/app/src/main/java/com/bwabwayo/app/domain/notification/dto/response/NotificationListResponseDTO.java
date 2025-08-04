package com.bwabwayo.app.domain.notification.dto.response;

import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationListResponseDTO {
    int size;
    @Builder.Default
    List<NotificationDTO> results = new ArrayList<>();

    public static NotificationListResponseDTO fromEntity(List<Notification> notifications){
        List<NotificationDTO> results = notifications.stream()
                .map(notification->{
                    Product product = notification.getProduct();

                    return NotificationDTO.builder()
                            .title(product.getTitle())
                            .thumbnail(product.getThumbnail())
                            .type(notification.getType().getTitle())
                            .createdAt(notification.getCreatedAt())
                            .build();
                }).toList();
        return NotificationListResponseDTO.builder()
                .size(results.size())
                .results(results)
                .build();
    }
}
