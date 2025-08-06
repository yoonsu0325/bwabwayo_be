package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.dto.response.ProductWithWishDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductRepositoryCustom {
    /**
     * 상품 조회
     */
    Page<ProductWithWishDTO> searchByCondition(
            String keyword,
            List<Long> categoryIds,
            Pageable pageable,
            String loginUserId,
            String sellerId,
            Boolean canVideoCall,
            Boolean canNegotiate,
            Boolean canDelivery,
            Boolean canDirect
    );
}
