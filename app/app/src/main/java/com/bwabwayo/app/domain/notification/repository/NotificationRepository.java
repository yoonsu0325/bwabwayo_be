package com.bwabwayo.app.domain.notification.repository;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.notification.domain.Notification;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.user.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Notification getNotificationById(Long id);

    void deleteAllByReceiverId(String receiverId);

    List<Notification> findAllByReceiverId(String receiverId);

    void deleteAllByReceiverIdAndProductId(String receiverId, Long productId);

    List<Notification> findAllByReceiverIdAndIsReadFalseAndUpdatedAtAfter(String receiverId, LocalDateTime createdAtAfter);

    List<Notification> findAllByReceiverIdAndIsReadFalseOrderByUpdatedAtDesc(String receiverId);


    // 채팅 생성
    @Modifying
    @Query(value = """
INSERT INTO notification
(receiver_id, product_id, chatroom_id, message, updated_at, is_read, unread_count)
VALUES (:receiverId, NULL, :roomId, :message, NOW(3), false, :initUnread)
ON DUPLICATE KEY UPDATE
  message = VALUES(message),
  updated_at = NOW(3),
  is_read = false,
  unread_count = CASE WHEN VALUES(unread_count) = 0 THEN unread_count ELSE unread_count + 1 END
""", nativeQuery = true)
    void upsertChat(@Param("receiverId") String receiverId,
                    @Param("roomId") Long roomId,
                    @Param("message") String message,
                    @Param("initUnread") int initUnread /* 0=발신자, 1=수신자 */);

    // 상품 알림 생성
    @Modifying
    @Query(value = """
INSERT INTO notification
(receiver_id, product_id, chatroom_id, message, updated_at, is_read, unread_count)
VALUES (:receiverId, :productId, NULL, :message, NOW(3), false, 1)
ON DUPLICATE KEY UPDATE
  message = VALUES(message),
  updated_at = NOW(3),
  is_read = false,
  unread_count = unread_count + 1
""", nativeQuery = true)
    void upsertProduct(@Param("receiverId") String receiverId,
                       @Param("productId") Long productId,
                       @Param("message") String message);



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
where n.receiver.id = :receiverId
order by n.updatedAt desc
""")
    Page<Notification> findInbox(@Param("receiverId") String receiverId, Pageable pageable);

    List<Notification> findAllByReceiverAndChatRoom(User receiver, ChatRoom chatRoom);

    List<Notification> findAllByReceiverAndProduct(User receiver, Product product);
}
