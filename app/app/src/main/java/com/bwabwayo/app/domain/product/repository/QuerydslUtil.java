package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.domain.QProduct;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Sort;

public class QuerydslUtil {
    public static OrderSpecifier<?>[] convertSort(Sort sort, QProduct product) {
        return sort.stream()
                .map(order -> {
                    PathBuilder<Product> path = new PathBuilder<>(Product.class, "product");

                    return order.isAscending()
                            ? path.getComparable(order.getProperty(), Comparable.class).asc()
                            : path.getComparable(order.getProperty(), Comparable.class).desc();
                })
                .toArray(OrderSpecifier[]::new);
    }
}