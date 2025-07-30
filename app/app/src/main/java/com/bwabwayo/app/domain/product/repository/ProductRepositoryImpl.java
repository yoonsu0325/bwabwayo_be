package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.domain.QProduct;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchByCondition(String keyword, List<Long> categoryIds, @NonNull Pageable pageable){
        QProduct product = QProduct.product;

        BooleanBuilder condition = new BooleanBuilder();

        // 키워드 검색
        if (categoryIds != null && !categoryIds.isEmpty()) {
            condition.and(product.category.id.in(categoryIds));
        }
        
        // 카테고리 필터링
        if (categoryIds != null && !categoryIds.isEmpty()) {
            condition.and(product.category.id.in(categoryIds));
        }

        List<Product> content = queryFactory
                .selectFrom(product)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslUtil.convertSort(pageable.getSort(), product))
                .fetch();

        @SuppressWarnings("DataFlowIssue")
        long total = queryFactory
                .select(product.count())
                .from(product)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
