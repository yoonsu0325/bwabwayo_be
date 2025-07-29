package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("최상위 카테고리만 조회")
    void findByParentIsNull() {
        // given
        Category root1 = Category.builder().id(1L).name("전자기기").build();
        Category root2 = Category.builder().id(2L).name("패션").build();

        Category child1 = Category.builder().id(3L).name("노트북").parent(root1).build();
        Category child2 = Category.builder().id(4L).name("남성의류").parent(root2).build();

        root1.setChildren(List.of(child1));
        root2.setChildren(List.of(child2));

        categoryRepository.saveAll(List.of(root1, root2));

        // when
        List<Category> result = categoryRepository.findByParentIsNull();

        // then
        assertThat(result)
                .hasSize(2)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("전자기기", "패션");
    }
}