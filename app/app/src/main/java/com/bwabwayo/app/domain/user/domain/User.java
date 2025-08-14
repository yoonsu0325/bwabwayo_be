package com.bwabwayo.app.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_user_nickname", columnNames = {"nickname"}),
                @UniqueConstraint(name = "unique_user_phone", columnNames = {"phone_number"}),
                @UniqueConstraint(name = "unique_user_email", columnNames = {"email"})
        }
)
public class User {

    @Id
    @Column(nullable = false)
    private String id; // 소셜 로그인 공급자 ID (ex. kakao_12345)

    @Version
    private Long version;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", unique = true, length = 11)
    private String phoneNumber;

    @Column(name = "profile_image", length = 2083)
    private String profileImage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int point;

    @Column(name = "deal_count", nullable = false)
    private int dealCount;

    @Column(name = "penalty_count", nullable = false)
    private int penaltyCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_login_at", nullable = false)
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
