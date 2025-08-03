package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Product;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    Product getProductById(Long id);

    @Modifying
    @Query("UPDATE Product p SET p.wishCount = p.wishCount + 1 WHERE p.id = :productId")
    void increaseWishCount(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE Product p SET p.wishCount = p.wishCount - 1 WHERE p.id = :productId AND p.wishCount > 0")
    void decreaseWishCount(@Param("productId") Long productId);
}
