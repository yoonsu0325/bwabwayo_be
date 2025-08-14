package com.bwabwayo.app.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpsertRequest {
    private String receiverId;
    private String message;

    public static UpsertRequest of(String receiverId, String message){
        return UpsertRequest.builder()
                .receiverId(receiverId)
                .message(message)
                .build();
    }
}
