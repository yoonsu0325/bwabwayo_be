package com.bwabwayo.app.domain.notification.service;

import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.notification.dto.response.NotificationDTO;
import com.bwabwayo.app.domain.notification.dto.response.NotificationListResponseDTO;
import com.bwabwayo.app.domain.notification.repository.NotificationRepository;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StorageService storageService;

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
}
