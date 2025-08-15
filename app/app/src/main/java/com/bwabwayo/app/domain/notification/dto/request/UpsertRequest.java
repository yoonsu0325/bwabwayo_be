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
    private Long productId;
    private Long chatroomId;
    private String message;

    public static UpsertRequest of(String receiverId, Long productId, Long chatroomId, String message){
        return UpsertRequest.builder()
                .receiverId(receiverId)
                .productId(productId)
                .chatroomId(chatroomId)
                .message(message)
                .build();
    }
}
