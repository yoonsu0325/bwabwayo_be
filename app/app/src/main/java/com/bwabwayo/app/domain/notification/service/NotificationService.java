package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.notification.dto.response.NotificationDTO;
import com.bwabwayo.app.domain.notification.dto.response.NotificationListResponseDTO;
import com.bwabwayo.app.domain.notification.repository.NotificationRepository;
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

    @Transactional(readOnly = true)
    public NotificationListResponseDTO getAllMyUnreadNotifications(String userId, boolean onlyUnread){
        List<Notification> notifications;
        if(onlyUnread){
            notifications = notificationRepository.findAllByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        } else{
            notifications = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(userId);
        }

        NotificationListResponseDTO responseDTO = NotificationListResponseDTO.fromEntity(notifications);
        for (NotificationDTO notificationDTO : responseDTO.getResults()) {
            notificationDTO.setThumbnail(storageService.getUrlFromKey(notificationDTO.getThumbnail()));
        }
        return responseDTO;
    }

    @Transactional
    public void setReadByNotificationId(Long notificationId, String userId){
        Notification notification = notificationRepository.getNotificationById(notificationId);

        if(!notification.getReceiver().getId().equals(userId)){
            throw new ForbiddenException("본인이 수신한 알림만 읽음 처리할수 있습니다.");
        }

        if(notification.isRead()) {
            log.warn("이미 읽은 알림 입니다: notificationId={}", notificationId);
            return;
        }
        notification.setRead(true);
    }

    @Transactional
    public void setReadAll(String userId){
        List<Notification> notifications = notificationRepository.getNotificationsByReceiverIdAndIsReadFalse(userId);
        for (Notification notification : notifications){
            notification.setRead(true);
        }
    }
}
