package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.dto.ProductQueryCondition;
import com.bwabwayo.app.domain.product.dto.response.ProductWithIsLikeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductRepositoryCustom {
    Page<ProductWithIsLikeDTO> searchByCondition(ProductQueryCondition condition, Pageable pageable);

    List<ProductWithIsLikeDTO> findByIdsInOrder(List<Long> ids, String viewerId);

    long getCount(ProductQueryCondition condition);
}
