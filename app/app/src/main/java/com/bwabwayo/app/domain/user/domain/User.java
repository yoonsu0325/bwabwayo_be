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
@Table(name = "user")
public class User {

    @Id
    @Column(nullable = false, length = 255)
    private String id; // 소셜 로그인 공급자 ID (ex. kakao_12345)

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", unique = true, length = 11)
    private String phoneNumber;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bio = "";

    @Column(nullable = false)
    private int score = 50;

    @Column(nullable = false)
    private int point = 0;

    @Column(name = "deal_count", nullable = false)
    private int dealCount = 0;

    @Column(name = "penalty_count", nullable = false)
    private int penaltyCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_login_at", nullable = false)
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken; // 암호화된 상태로 저장
}
