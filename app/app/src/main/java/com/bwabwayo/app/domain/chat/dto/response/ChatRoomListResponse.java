package com.bwabwayo.app.domain.chat.dto.response;

import com.bwabwayo.app.domain.chat.domain.ChatMessageRedisEntity;
import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.domain.user.domain.User;
import lombok.*;

import java.io.Serializable;

@Getter @Setter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class ChatRoomListResponse implements Serializable {

    private Long roomId;

    private String buyerId;

    private String sellerId;

    private Long productId;

    private String productName;

    private Integer productPrice;

    private String userId;

    private String sellerProfileImageUrl;

    private String buyerProfileImageUrl;

    private String productImageUrl;

    private String myNickName;

    private String partnerNickName;

    private SaleStatus saleStatus;

    private MessageDTO lastChatmessageDto;

    private Long unreadCount;

    public static ChatRoomListResponse fromInitial(ChatRoom room, String userId, User seller, User buyer, Product product) {
        boolean isBuyer = room.getBuyerId().equals(userId);
        String userNickname = isBuyer ? buyer.getNickname() : seller.getNickname();
        String partnerNickname = isBuyer ? seller.getNickname() : buyer.getNickname();


        return ChatRoomListResponse.builder()
                .roomId(room.getRoomId())
                .buyerId(room.getBuyerId())
                .sellerId(room.getSellerId())
                .productId(room.getProductId())
                .productName(product.getTitle())
                .productPrice(product.getPrice())
                .userId(userId)
                .sellerProfileImageUrl(seller.getProfileImage())
                .buyerProfileImageUrl(buyer.getProfileImage())
                .productImageUrl(product.getThumbnail())
                .myNickName(userNickname)
                .partnerNickName(partnerNickname)
                .saleStatus(product.getSaleStatus())
                .lastChatmessageDto(null)
                .unreadCount(0L)
                .build();
    }

    public void updateChatMessageDto(MessageDTO chatMessageDto) {
        this.lastChatmessageDto = chatMessageDto;
    }

    public void changePartnerInfo() {
        String tmp = myNickName;
        this.myNickName = partnerNickName;
        this.partnerNickName = tmp;

        if (this.userId.equals(sellerId)) {
            this.userId = buyerId;
        } else if (this.userId.equals(buyerId)) {
            this.userId = sellerId;
        }
    }

    public void updateLastMessageInfo(ChatMessageRedisEntity msg) {
        System.out.println(msg.getContent());
        System.out.println(msg.getIsRead());

        this.lastChatmessageDto = MessageDTO.fromEntity(msg);
    }
}
