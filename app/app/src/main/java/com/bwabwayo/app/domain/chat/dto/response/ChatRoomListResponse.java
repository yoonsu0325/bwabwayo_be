package com.bwabwayo.app.domain.chat.dto.response;

import com.bwabwayo.app.domain.chat.domain.ChatMessageRedisEntity;
import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.user.domain.ReviewAgg;
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

    private String userId;

    BuyerInfoResponse buyer;

    SellerInfoResponse seller;

    ProductInfoResponse product;

    private MessageDTO lastMessage;

    private Long unreadCount;

    private String userNickname;

    private String partnerNickname;

    public static ChatRoomListResponse fromInitial(
            ChatRoom room, String userId,
            User seller, ReviewAgg sellerReview, User buyer,
            Product product, String productImageUrl) {

        boolean isBuyer = room.getBuyerId().equals(userId);
        String userNickname = isBuyer ? buyer.getNickname() : seller.getNickname();
        String partnerNickname = isBuyer ? seller.getNickname() : buyer.getNickname();

        return ChatRoomListResponse.builder()
                .roomId(room.getRoomId())
                .userId(userId)
                .buyer(BuyerInfoResponse.from(buyer))
                .seller(SellerInfoResponse.from(seller, sellerReview))
                .product(ProductInfoResponse.from(product, productImageUrl))
                .lastMessage(null)
                .unreadCount(0L)
                .userNickname(userNickname)
                .partnerNickname(partnerNickname)
                .build();
    }

    public void updateChatMessageDto(MessageDTO chatMessageDto) {
        this.lastMessage = chatMessageDto;
    }

    public void changePartnerInfo() {
        String tmp = userNickname;
        this.userNickname = partnerNickname;
        this.partnerNickname = tmp;

        if (this.userId.equals(seller.getId())) {
            this.userId = buyer.getId();
        } else if (this.userId.equals(buyer.getId())) {
            this.userId = seller.getId();
        }
    }

    public void updateLastMessageInfo(ChatMessageRedisEntity msg) {
        System.out.println(msg.getContent());
        System.out.println(msg.getIsRead());

        this.lastMessage = MessageDTO.fromEntity(msg);
    }
}
