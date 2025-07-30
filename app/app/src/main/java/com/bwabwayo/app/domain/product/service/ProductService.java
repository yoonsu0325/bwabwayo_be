package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductSearchResponseDTO;
import com.bwabwayo.app.domain.product.repository.CategoryRepository;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final S3Service s3Service;

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
        
        // 페이지네이션
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        // 하위 카테고리 포함 카테고리 ID
        Category topCategory = categoryService.getCategoryById(categoryId);
        List<Long> categoryIds = new ArrayList<>();
        getSubCategoryIds(topCategory, categoryIds);
        
        // DB 조회
        Page<Product> pageData = productRepository.searchByCondition(keyword, categoryIds, pageable);
        
        // thumbnail을 URL로 확장
        pageData.getContent().forEach(p-> p.setThumbnail(s3Service.getUrl(p.getThumbnail())));

        return ProductSearchResponseDTO.fromEntity(pageData);
    }

    /**
     * 현재 카테고리와 그 하위의 카테고리의 ID의 리스트 생성
     */
    private void getSubCategoryIds(Category category, List<Long> result){
        if(category == null) return;

        result.add(category.getId());
        for(Category subCategory : category.getChildren()){
            getSubCategoryIds(subCategory, result);
        }
    }
}