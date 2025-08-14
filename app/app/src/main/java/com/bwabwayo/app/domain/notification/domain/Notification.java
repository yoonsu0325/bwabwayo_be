package com.bwabwayo.app.domain.notification.domain;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(
        name = "notification",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_receiver_chatroom", columnNames = {"receiver_id", "chatroom_id"}),
                @UniqueConstraint(name = "uk_receiver_product", columnNames = {"receiver_id", "product_id"})
        },
        indexes = {
                @Index(name = "idx_receiver_updatedAt", columnList = "receiver_id, updated_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false, updatable = false)
    private User receiver; // 사용자에 관한 알림에 필수 (예: 공지사항, 혜택/처벌 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", updatable = false)
    private Product product; // 상품에 관한 알림에 필수 (예: 찜한 상품의 가격 변경)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", updatable = false)
    private ChatRoom chatRoom; // 채팅에 관한 알림에 필수 (예: 메시지 수신)

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Column(nullable = false)
    @Builder.Default
    private int unreadCount = 0;

    @PrePersist
    public void prePersist(){
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
