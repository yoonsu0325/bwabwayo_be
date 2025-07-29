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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    public ProductSearchResponseDTO searchProducts(ProductSearchRequestDTO requestDTO) {
        String keyword = requestDTO.getKeyword();
        Long categoryId = requestDTO.getCategoryId();
        Integer page = requestDTO.getPage();
        Integer size = requestDTO.getSize();

        Sort sort = Sort.by("createdAt").descending(); // 최신순 조회
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> pageData = productRepository.searchByCondition(keyword, categoryId, pageable);

        return ProductSearchResponseDTO.fromEntity(pageData);
    }
}