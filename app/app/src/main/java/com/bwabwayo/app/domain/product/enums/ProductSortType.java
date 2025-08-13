package com.bwabwayo.app.domain.product.enums;

import com.bwabwayo.app.domain.product.util.SortUtils;
import lombok.Getter;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

@Getter
public enum ProductSortType {
    LATEST("latest", SortUtils.withIdDesc("createdAt", Sort.Direction.DESC)),
    OLDEST("oldest", SortUtils.withIdDesc("createdAt", Sort.Direction.ASC)),
    PRICE_ASC("price_asc", SortUtils.withIdDesc("price", Sort.Direction.ASC)),
    PRICE_DESC("price_desc", SortUtils.withIdDesc("price", Sort.Direction.DESC)),
    VIEWS_DESC("views_desc", SortUtils.withIdDesc("viewCount", Sort.Direction.DESC)),
    WISH_DESC("wishes_desc", SortUtils.withIdDesc("wishCount", Sort.Direction.DESC)),
    RELATED("related", Sort.by("id").descending()),
    LATEST_AND_RELATED("latest_and_related", SortUtils.withIdDesc("createdAt", Sort.Direction.DESC));

    private final String queryValue;
    private final Sort sort;

    ProductSortType(String queryValue, Sort sort) {
        this.queryValue = queryValue;
        this.sort = sort;
    }

    public static ProductSortType from(String value) {
        return Arrays.stream(values())
                .filter(v -> v.queryValue.equalsIgnoreCase(value))
                .findFirst()
                .orElse(LATEST);
    }
}
