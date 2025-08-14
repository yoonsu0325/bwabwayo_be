package com.bwabwayo.app.domain.ai.controller;


import com.bwabwayo.app.domain.ai.domain.AiCategoryTemplate;
import com.bwabwayo.app.domain.ai.repository.AiCategoryTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/ai/categories")
@RequiredArgsConstructor
public class AiCategoryController {

    private final AiCategoryTemplateRepository aiCategoryTemplateRepository;

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getAiCategoryTemplate(
            @PathVariable Long categoryId) {
        AiCategoryTemplate aiCategoryTemplate = aiCategoryTemplateRepository.findById(categoryId).orElseThrow();

        return ResponseEntity.ok().body(aiCategoryTemplate);
    }

}
