package com.bwabwayo.app.domain.notification.dto.response;

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

    public static NotificationListResponseDTO of(List<NotificationDTO> notificationDTOs) {
        return NotificationListResponseDTO.builder()
                .size(notificationDTOs.size())
                .results(notificationDTOs)
                .build();
    }
}
