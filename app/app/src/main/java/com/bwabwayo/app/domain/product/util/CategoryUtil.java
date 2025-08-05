package com.bwabwayo.app.domain.product.util;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.dto.response.CategoryDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryUtil {
    /**
     * 현재 카테고리와 그 하위의 카테고리의 리스트 생성
     */
    public static List<Category> getSubCategories(Category category){
        List<Category> result = new ArrayList<>();
        getSubCategoriesImpl(category, result);
        return result;
    }

    private static void getSubCategoriesImpl(Category category, List<Category> result){
        if(category == null) return;

        result.add(category);
        for(Category subCategory : category.getChildren()){
            getSubCategoriesImpl(subCategory, result);
        }
    }

    /**
     * 현재 카테고리와 모든 선조 카테고리의 리스트 생성
     */
    public static List<Category> getSupperCategories(Category category) {
        List<Category> result = new ArrayList<>();

        while (category != null) {
            result.add(category);
            category = category.getParent();
        }
        Collections.reverse(result);
        return result;
    }
}
