package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.dto.ProductQueryCondition;
import com.bwabwayo.app.domain.product.dto.response.ProductWithIsLikeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    /**
     * 상품 조회
     */
    Page<ProductWithIsLikeDTO> searchByCondition(ProductQueryCondition condition, Pageable pageable);
}
