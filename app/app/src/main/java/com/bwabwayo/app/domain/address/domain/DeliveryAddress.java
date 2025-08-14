package com.bwabwayo.app.domain.address.domain;

import com.bwabwayo.app.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 배송지 ID (AI)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 사용자 ID (외래키)

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;  // 수령인 이름

    @Column(name = "recipient_phone_number", nullable = false, length = 11)
    private String recipientPhoneNumber;  // 수령인 연락처

    @Column(name = "zipcode", nullable = false, length = 5)
    private String zipcode;  // 우편번호

    @Column(name = "address", nullable = false)
    private String address;  // 기본 주소

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;  // 상세 주소
}

