package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 최상위 카테고리 조회
     */
    @Transactional(readOnly = true)
    public List<Category> getTopCategories() {
        return categoryRepository.findByParentIsNull();
    }

    /**
     * 카테고리 가져오기
     */
    public Category getCategoryById(Long categoryId){
        return categoryRepository.getCategoryById(categoryId)
                .orElseThrow(()-> new EntityNotFoundException("카테고리가 존재하지 않습니다."));
    }
}
