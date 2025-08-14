package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    Courier findByCode(String courierCode);
}
