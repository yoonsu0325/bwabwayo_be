package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRepository;
import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.notification.dto.response.NotificationDTO;
import com.bwabwayo.app.domain.notification.repository.NotificationRepository;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.domain.user.service.UserService;
import com.bwabwayo.app.global.exception.BadRequestException;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StorageService storageService;
    private final UserService userService;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ProductRepository productRepository;


    @Transactional
    public void upsertChat(String receiverId, Long roomId, String message, int initUnread){
        notificationRepository.upsertChat(receiverId, roomId, message, initUnread);
    }

    @Transactional
    public void upsertProduct(String receiverId, Long productId, String message){
        notificationRepository.upsertProduct(receiverId, productId, message);
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

    public NotificationDTO build(Notification notification){
        User receiver = notification.getReceiver();
        ChatRoom chatRoom = notification.getChatRoom();
        Product product = notification.getProduct();

        NotificationDTO.NotificationDTOBuilder builder = NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .unreadCount(notification.getUnreadCount())
                .createdAt(notification.getUpdatedAt());

        if(chatRoom != null){ // 채팅
            User other = userService.findById(chatRoom.getOtherUserId(receiver.getId()));
            if(product == null) product = productService.findById(chatRoom.getProductId());
            builder.title(product.getTitle() + " from. " + other.getNickname())
                    .thumbnail(storageService.getUrlFromKey(product.getThumbnail()));
        } else if(product != null){ // 상품
            builder.title(product.getTitle())
                .thumbnail(storageService.getUrlFromKey(product.getThumbnail()));
        } else{ // 사용자
            builder.title("to. " + receiver.getNickname())
                    .thumbnail(storageService.getUrlFromKey(receiver.getProfileImage()));
        }

        return builder.build();
    }

    public Notification findByChat(String receiverId, Long chatId) {
        User receiver = userRepository.findById(receiverId).orElseThrow(()-> new BadRequestException("수신자 없음: reciverId="+receiverId));
        ChatRoom chatRoom = chatRoomRepository.findById(chatId).orElseThrow(()-> new BadRequestException("채팅방 없음: chatroomId="+chatId));
        return notificationRepository.findAllByReceiverAndChatRoom(receiver, chatRoom).get(0);
    }

    public Notification findByProduct(String receiverId, Long productId) {
        User receiver = userRepository.findById(receiverId).orElseThrow(()-> new BadRequestException("수신자 없음: reciverId="+receiverId));
        Product product = productRepository.findById(productId).orElseThrow(()-> new BadRequestException("상품 없음: productId="+productId));
        return notificationRepository.findAllByReceiverAndProduct(receiver, product).get(0);
    }
}
