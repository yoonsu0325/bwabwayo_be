package com.bwabwayo.app.domain.chat.dto;

import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
import lombok.*;

import java.util.List;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageSubDTO {
    private String userId;
    private String partnerId;
    private MessageDTO messageDTO;
    private List<ChatRoomListResponse> list;
    private List<ChatRoomListResponse> partnerList;
}
