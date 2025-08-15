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
public class NotificationListResponse {
    int size;

    @Builder.Default
    List<NotificationResponse> results = new ArrayList<>();

    public static NotificationListResponse of(List<NotificationResponse> notificationDTOs) {
        return NotificationListResponse.builder()
                .size(notificationDTOs.size())
                .results(notificationDTOs)
                .build();
    }
}
