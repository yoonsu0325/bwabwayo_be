package com.bwabwayo.app.domain.notification.repository;

import com.bwabwayo.app.domain.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(String receiverId);

    List<Notification> findAllByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(String receiverId);

    Notification getNotificationById(Long id);

    List<Notification> getNotificationsByReceiverIdAndIsReadFalse(String receiverId);

    List<Notification> findAllByReceiverIdAndIsReadFalseAndCreatedAtAfter(String receiverId, LocalDateTime createdAtAfter);
}
