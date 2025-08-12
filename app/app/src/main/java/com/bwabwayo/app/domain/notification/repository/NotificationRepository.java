package com.bwabwayo.app.domain.notification.repository;

import com.bwabwayo.app.domain.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Notification getNotificationById(Long id);

    void deleteAllByReceiverId(String receiverId);

    List<Notification> findAllByReceiverId(String receiverId);

    void deleteAllByReceiverIdAndProductId(String receiverId, Long productId);
}
