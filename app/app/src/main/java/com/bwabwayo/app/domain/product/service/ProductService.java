package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductSearchResponseDTO;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * 상품 검색
     */
    public ProductSearchResponseDTO searchProducts(ProductSearchRequestDTO requestDTO) {
        String keyword = requestDTO.getKeyword();
        Long categoryId = requestDTO.getCategoryId();
        int page = requestDTO.getPage();
        int size = requestDTO.getSize();

        // 최신순 정렬; ID순 정렬
        Sort sort = Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.asc("id")
        );

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> pageData = productRepository.searchByCondition(keyword, categoryId, pageable);

        return ProductSearchResponseDTO.fromEntity(pageData);
    }
}