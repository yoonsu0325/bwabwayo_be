package com.bwabwayo.app.domain.address.repository;

import com.bwabwayo.app.domain.address.domain.DeliveryAddress;
import com.bwabwayo.app.domain.address.dto.response.DeliveryAddressResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    // 특정 사용자의 모든 배송지 목록 조회
//    List<DeliveryAddress> findAllByUserId(String userId);

    DeliveryAddress findTopByUser_Id(String userId);

    Page<DeliveryAddress> findAllByUser_Id(String userId, Pageable pageable);
}

