package com.bwabwayo.app.domain.support.domain;


import com.bwabwayo.app.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Report{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    public String title;

    @Setter(AccessLevel.NONE)
    @Builder.Default
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportImage> reportImages = new ArrayList<>();

    @Column(nullable = false)
    public String description;

    @Column(columnDefinition = "TEXT")
    private String reply; // 신고 답변

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, updatable = false)
    public User reporter; // 신고자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false, updatable = false)
    public User target; // 신고 대상자

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 신고 시각

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

}
