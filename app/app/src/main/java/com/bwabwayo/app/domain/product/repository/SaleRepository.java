package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Sale;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    @Query("""
    SELECT s FROM Sale s
    JOIN FETCH s.product p
    LEFT JOIN FETCH p.courier c
    WHERE s.buyerId = :buyerId
    """)
    List<Sale> findWithProductAndCourierByBuyerId(@Param("buyerId") String buyerId);
}
