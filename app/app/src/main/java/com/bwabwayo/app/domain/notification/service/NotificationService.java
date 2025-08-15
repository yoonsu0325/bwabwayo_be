package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.notification.dto.response.NotificationResponse;
import com.bwabwayo.app.domain.notification.repository.NotificationRepository;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.service.UserService;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StorageService storageService;
    private final UserService userService;
    private final ProductService productService;


    // ===================== upsert =======================

    @Transactional
    public void upsert(String receiverId, Long productId, Long roomId, String message){
        notificationRepository.upsert(receiverId, productId, roomId, message);
    }

    // ===================== mark-read =======================

    @Transactional
    public void markRead(String receiverId, Long notificationId){
        notificationRepository.markRead(receiverId, notificationId);
    }

    @Transactional
    public void markChatRead(String receiverId, Long roomId){
        notificationRepository.markChatRead(receiverId, roomId);
    }

    @Transactional
    public void markProductRead(String receiverId, Long roomId){
        notificationRepository.markProductRead(receiverId, roomId);
    }

    @Transactional
    public Page<Notification> findInbox(String receiverId, Pageable pageable){
        return notificationRepository.findInbox(receiverId, pageable);
    }

    public NotificationResponse build(Notification notification){
        User receiver = notification.getReceiver();
        ChatRoom chatRoom = notification.getChatRoom();
        Product product = notification.getProduct();

        var builder = NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .unreadCount(notification.getUnreadCount())
                .createdAt(notification.getUpdatedAt())
                .receiverId(receiver.getId());

        if(chatRoom != null){ // 채팅
            User other = userService.findById(chatRoom.getOtherUserId(receiver.getId()));
            if(product == null) product = productService.findById(chatRoom.getProductId());
            builder.title(product.getTitle() + " from. " + other.getNickname())
                    .thumbnail(storageService.getUrlFromKey(product.getThumbnail()))
                    .productId(product.getId())
                    .chatroomId(chatRoom.getRoomId());
        } else if(product != null){ // 상품
            builder.title(product.getTitle())
                .thumbnail(storageService.getUrlFromKey(product.getThumbnail()))
                .productId(product.getId());
        } else{ // 사용자
            builder.title("to. " + receiver.getNickname())
                    .thumbnail(storageService.getUrlFromKey(receiver.getProfileImage()));
        }

        return builder.build();
    }
}
