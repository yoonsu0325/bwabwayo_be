package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ProductRepositoryCustom {
    /**
     * 상품 조회
     */
    Page<Product> searchByCondition(String keyword, Long categoryId, Pageable pageable);
}
