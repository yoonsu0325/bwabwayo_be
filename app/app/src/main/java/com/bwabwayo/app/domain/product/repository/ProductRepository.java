package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.enums.DeliveryStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    Product getProductById(Long id);

    @Modifying
    @Query("UPDATE Product p SET p.wishCount = p.wishCount + 1 WHERE p.id = :productId")
    void increaseWishCount(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE Product p SET p.wishCount = p.wishCount - 1 WHERE p.id = :productId AND p.wishCount > 0")
    void decreaseWishCount(@Param("productId") Long productId);

    List<Product> findAllByDeliveryStatus(DeliveryStatus deliveryStatus);

    @Query("SELECT p FROM Product p WHERE p.invoiceNumber = '' AND p.deliveryStatus = :deliveryStatus")
    List<Product> findAllByEmptyInvoiceNumberAndDeliveryStatus(@Param("deliveryStatus") DeliveryStatus deliveryStatus);

}
