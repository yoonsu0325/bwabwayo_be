package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.dto.response.CategoryAllResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.CategoryTreeDTO;
import com.bwabwayo.app.domain.product.exception.CategoryNotFoundException;
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
    public CategoryAllResponseDTO getTopCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryTreeDTO> categoryTreeDTOs = buildCategoryTree(categories);

        return CategoryAllResponseDTO.builder()
                .totalCategories(categories.size())
                .totalTopCategories(categoryTreeDTOs.size())
                .categories(categoryTreeDTOs)
                .build();
    }

    /**
     * 카테고리 가져오기
     */
    @Transactional(readOnly = true)
    public Category findById(Long categoryId){
        return  categoryRepository.findById(categoryId)
                .orElseThrow(()->new CategoryNotFoundException(categoryId));

    }

    /**
     * 카테고리 존재 여부 확인
     */
    public boolean existsById(Long categoryId){
        return categoryRepository.existsById(categoryId);
    }


    /**
     * 카테고리의 flatList를 tree로 재구성
     */
    private List<CategoryTreeDTO> buildCategoryTree(List<Category> flatList) {
        List<CategoryTreeDTO> roots = new ArrayList<>();

        Map<Long, CategoryTreeDTO> dtoMap = new HashMap<>();

        // DTOMap과 EntityMap 초기화
        for (Category category : flatList) {
            CategoryTreeDTO dto = CategoryTreeDTO.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .build();
            dtoMap.put(category.getId(), dto);
        }

        // 트리 구성
        for (Category category : flatList) {
            CategoryTreeDTO dto = dtoMap.get(category.getId());
            Category parent = category.getParent();

            if (parent == null) {
                roots.add(dto);
            } else {
                Long parentId = parent.getId();
                CategoryTreeDTO parentDto = dtoMap.get(parentId);
                parentDto.getSubCategories().add(dto);
            }
        }

        return roots;
    }
}
