package com.bwabwayo.app.domain.notification.repository;

import com.bwabwayo.app.domain.notification.domain.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 알림 생성
    @Modifying
    @Query(value = """
INSERT INTO notification
(receiver_id, product_id, chatroom_id, message, updated_at, is_read, unread_count)
VALUES (:receiverId, :productId, :roomId, :message, :updatedAt, false, 1)
ON DUPLICATE KEY UPDATE
  message = VALUES(message),
  updated_at = :updatedAt,
  is_read = false,
  unread_count = unread_count + 1
""", nativeQuery = true)
    void upsert(@Param("receiverId") String receiverId,
                @Param("productId") Long productId,
                @Param("roomId") Long roomId,
                @Param("message") String message,
                @Param("updatedAt") LocalDateTime updatedAt
    );

    // 알림 읽음 표시
    @Modifying
    @Query("""
update Notification n
set n.isRead = true, n.unreadCount = 0
where n.receiver.id = :receiverId and n.id = :notificationId
""")
    void markRead(@Param("receiverId") String receiverId, @Param("notificationId") Long notificationId);


    // 채팅 읽음 표시
    @Modifying
    @Query("""
update Notification n
set n.isRead = true, n.unreadCount = 0
where n.receiver.id = :receiverId and n.chatRoom.roomId = :roomId
""")
    void markChatRead(@Param("receiverId") String receiverId, @Param("roomId") Long roomId);

    // 상품 알림 읽음 표시
    @Modifying
    @Query("""
update Notification n
set n.isRead = true, n.unreadCount = 0
where n.receiver.id = :receiverId and n.product.id = :productId
""")
    void markProductRead(@Param("receiverId") String receiverId, @Param("productId") Long productId);

    // 인박스 가져오기
    @Query("""
select n
from Notification n
where n.receiver.id = :receiverId and n.unreadCount > 0
order by n.updatedAt desc
""")
    Page<Notification> findInbox(@Param("receiverId") String receiverId, Pageable pageable);
}
