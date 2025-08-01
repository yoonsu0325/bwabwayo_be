package com.bwabwayo.app.domain.chat.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequest {
    private String sellerId;
    private Long productId;
}
