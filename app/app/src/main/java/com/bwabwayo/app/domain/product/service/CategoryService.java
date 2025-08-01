package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.dto.response.CategoryTreeDTO;
import com.bwabwayo.app.domain.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 최상위 카테고리 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryTreeDTO> getTopCategories() {
        List<Category> categories = categoryRepository.findAll();
        return buildCategoryTree(categories);
    }

    /**
     * 카테고리 가져오기
     */
    public Category getCategoryById(Long categoryId){
        return  categoryRepository.getCategoryById(categoryId);
    }

    /**
     * 카테고리 존재 여부 확인
     */
    public boolean existsCategoryById(Long categoryId){
        return categoryRepository.existsCategoryById(categoryId);
    }



    /**
     * flat한 카테고리를 tree로 변환
     */
    private List<CategoryTreeDTO> buildCategoryTree(List<Category> flatList) {
        // id → DTO 매핑
        Map<Long, CategoryTreeDTO> dtoMap = new HashMap<>();
        // id → Entity 매핑 (부모 ID 확인용)
        Map<Long, Category> entityMap = new HashMap<>();

        List<CategoryTreeDTO> roots = new ArrayList<>();

        // DTO 초기 생성 및 매핑 저장
        for (Category category : flatList) {
            dtoMap.put(category.getId(), CategoryTreeDTO.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .build());
            entityMap.put(category.getId(), category);
        }

        // 트리 구성
        for (Category category : flatList) {
            CategoryTreeDTO dto = dtoMap.get(category.getId());
            Category parent = category.getParent();

            if (parent == null) {
                roots.add(dto);
            } else {
                Long parentId = parent.getId(); // parent는 실제로는 프록시일 수 있음
                CategoryTreeDTO parentDto = dtoMap.get(parentId);
                if (parentDto != null) {
                    parentDto.getSubCategories().add(dto);
                }
            }
        }

        return roots;
    }

}
