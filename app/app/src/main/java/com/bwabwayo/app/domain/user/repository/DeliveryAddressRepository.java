package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    // 특정 사용자의 모든 배송지 목록 조회
//    List<DeliveryAddress> findAllByUserId(String userId);
}

