package com.bwabwayo.app.domain.product.controller;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getTopCategories_success() throws Exception {
        // given
        Category childCategory = Category.builder().id(2L).name("노트북").build();
        Category parentCategory = Category.builder()
                .id(1L).name("전자제품").children(List.of(childCategory))
                .build();

        given(categoryService.getTopCategories()).willReturn(List.of(parentCategory));

        // when & then
        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("카테고리 조회에 성공했습니다."))
                .andExpect(jsonPath("$.categories[0].categoryId").value(1L))
                .andExpect(jsonPath("$.categories[0].categoryName").value("전자제품"))
                .andExpect(jsonPath("$.categories[0].subCategories[0].categoryName").value("노트북"));
    }

    @Test
    @DisplayName("카테고리 목록 조회 실패 - 서버 오류")
    void getTopCategories_fail() throws Exception {
        // given
        doThrow(new RuntimeException("DB error")).when(categoryService).getTopCategories();

        // when & then
        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isInternalServerError());
//                .andExpect(jsonPath("$.message").value("카테고리 조회 중 서버 오류가 발생했습니다."));
    }
}
