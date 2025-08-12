package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.chat.repository.ChatRoomRepository;
import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.notification.dto.response.NotificationDTO;
import com.bwabwayo.app.domain.notification.dto.response.NotificationListResponseDTO;
import com.bwabwayo.app.domain.notification.repository.NotificationRepository;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.domain.user.service.UserService;
import com.bwabwayo.app.global.exception.ForbiddenException;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StorageService storageService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;


    @Transactional(readOnly = true)
    public NotificationListResponseDTO findAllByReceiverId(String receiverId){
        List<Notification> list = notificationRepository.findAllByReceiverId(receiverId);
        List<NotificationDTO> dtoList = list.stream().map(this::build).toList();

        return NotificationListResponseDTO.builder()
                .size(dtoList.size())
                .results(dtoList)
                .build();
    }

    @Transactional
    public void readNotification(Long notificationId, String userId){
        Notification notification = notificationRepository.getNotificationById(notificationId);

        if(!notification.getReceiver().getId().equals(userId)){
            throw new ForbiddenException("본인이 수신한 알림만 읽음 처리할수 있습니다.");
        }

        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void readAllNotifications(String userId){
        notificationRepository.deleteAllByReceiverId(userId);
    }

    @Transactional
    public Notification send(String targetUserId, String message, Long productId){
        Product product = productRepository.getProductById(productId);
        User receiver = userService.findById(targetUserId);

        Notification notification = Notification.builder()
                .receiver(receiver)
                .product(product)
                .message(message)
                .build();

        notificationRepository.deleteAllByReceiverIdAndProductId(targetUserId, productId);
        notificationRepository.save(notification);

        return notification;
    }

    public NotificationDTO build(Notification notification){
        Product product = notification.getProduct();
        User receiver = notification.getReceiver();

        return NotificationDTO.builder()
                .id(notification.getId())
                .title(product.getTitle())
                .message(notification.getMessage())
                .thumbnail(storageService.getUrlFromKey(product.getThumbnail()))
                .build();
    }
}
