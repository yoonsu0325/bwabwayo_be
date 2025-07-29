package com.bwabwayo.app.domain.chat.domain;

import com.bwabwayo.app.domain.chat.dto.request.CreateChatRoomRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter @Setter
@ToString
@Entity
public class ChatRoom implements Serializable {
    @Serial
    private static final long serialVersionUID = 111111L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;
    private Long buyerId;
    private Long sellerId;
    private Long productId;

    public static ChatRoom createRoom(CreateChatRoomRequest request){
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.sellerId = request.getSellerId();
        chatRoom.buyerId = request.getBuyerId();
        chatRoom.productId = request.getProductId();
        return chatRoom;
    }

    public Long getOtherUserId(Long userId) {
        if(Objects.equals(userId, this.sellerId)) return buyerId;
        else return sellerId;
    }
}
