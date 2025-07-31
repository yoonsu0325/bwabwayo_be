package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull();

    Optional<Category> getCategoryById(Long categoryId);
}
