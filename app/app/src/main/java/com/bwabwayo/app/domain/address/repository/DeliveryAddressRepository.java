package com.bwabwayo.app.domain.address.repository;

import com.bwabwayo.app.domain.address.domain.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    // 특정 사용자의 모든 배송지 목록 조회
//    List<DeliveryAddress> findAllByUserId(String userId);

    DeliveryAddress findByUser_Id(String userId);
}

