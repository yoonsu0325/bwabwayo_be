package com.bwabwayo.app.domain.chat.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequest {
    private Long buyerId;
    private Long sellerId;
    private Long productId;
}
