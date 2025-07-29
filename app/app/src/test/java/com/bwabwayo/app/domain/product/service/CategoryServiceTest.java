package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("최상위 카테고리 목록 조회")
    void getTopCategories() {
        // given
        Category category1 = Category.builder().id(1L).name("전자제품").build();
        Category category2 = Category.builder().id(2L).name("의류").build();

        given(categoryRepository.findByParentIsNull())
                .willReturn(List.of(category1, category2));

        // when
        List<Category> result = categoryService.getTopCategories();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName)
                .containsExactly("전자제품", "의류");
    }
}
