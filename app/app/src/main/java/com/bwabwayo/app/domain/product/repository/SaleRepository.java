package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Sale;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    @EntityGraph(attributePaths = {"product", "product.courier"})
    Page<Sale> findWithProductAndCourierByBuyerId(String buyerId, Pageable pageable);
}
