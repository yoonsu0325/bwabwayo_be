package com.bwabwayo.app.domain.product.util;

import org.springframework.data.domain.Sort;

public class SortUtils {

    /**
     * 대표 정렬 기준 + ID DESC를 2차 기준으로 추가
     */
    public static Sort withIdDesc(Sort primarySort) {
        return primarySort.and(Sort.by(Sort.Direction.DESC, "id"));
    }

    /**
     * 대표 정렬 필드명 + ID DESC
     */
    public static Sort withIdDesc(String field, Sort.Direction direction) {
        return Sort.by(new Sort.Order(direction, field))
                   .and(Sort.by(Sort.Direction.DESC, "id"));
    }
}
