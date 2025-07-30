package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull();

    Category getCategoryById(Long categoryId);
}
