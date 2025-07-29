package com.bwabwayo.app.domain.chat.dto;

import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
import lombok.*;

import java.util.List;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageSubDTO {
    private Long userId;
    private Long partnerId;
    private MessageDTO messageDTO;
    private List<ChatRoomListResponse> list;
    private List<ChatRoomListResponse> partnerList;
}
