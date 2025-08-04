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
        List<NotificationDTO> results = notifications
                .stream()
                .map(NotificationDTO::fromEntity)
                .toList();

        return NotificationListResponseDTO.builder()
                .size(results.size())
                .results(results)
                .build();
    }
}
