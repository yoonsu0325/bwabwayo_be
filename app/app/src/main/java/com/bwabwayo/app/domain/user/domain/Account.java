package com.bwabwayo.app.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 계좌 ID (AI)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // 유니크하게 매핑
    private User user;  // 사용자 ID (외래키, 1:1 관계)

    @Column(name = "account_number", nullable = false)
    private String accountNumber;  // 계좌번호

    @Column(name = "account_holder", nullable = false)
    private String accountHolder;  // 예금주명

    @Column(name = "bank_name", nullable = false)
    private String bankName;  // 은행명
}

