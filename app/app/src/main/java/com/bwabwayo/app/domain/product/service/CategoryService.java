package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.repository.CategoryRepository;
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

    public Category getCategoryById(Long categoryId){
        return categoryRepository.getCategoryById(categoryId);
    }
}
