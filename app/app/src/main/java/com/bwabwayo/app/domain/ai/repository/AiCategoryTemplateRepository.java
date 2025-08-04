package com.bwabwayo.app.domain.ai.repository;

import com.bwabwayo.app.domain.ai.domain.AiCategoryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiCategoryTemplateRepository extends JpaRepository<AiCategoryTemplate, Long> {
}
