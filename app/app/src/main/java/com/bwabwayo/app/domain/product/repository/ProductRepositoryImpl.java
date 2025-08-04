package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.QProduct;
import com.bwabwayo.app.domain.product.dto.response.ProductWithWishDTO;
import com.bwabwayo.app.domain.user.domain.QUser;
import com.bwabwayo.app.domain.wish.domain.QWish;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
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
    public Page<ProductWithWishDTO> searchByCondition(String keyword, List<Long> categoryIds, @NonNull Pageable pageable, String loginUserId, String sellerId){
        QProduct product = QProduct.product;
        QWish wish = QWish.wish;

        BooleanBuilder condition = new BooleanBuilder();

        // 키워드 검색
        if (keyword != null && !keyword.isEmpty()) {
            condition.and(product.title.containsIgnoreCase(keyword));
        }
        
        // 카테고리 필터링
        if (categoryIds != null && !categoryIds.isEmpty()) {
            condition.and(product.category.id.in(categoryIds));
        }

        // 판매자 필터링
        if(sellerId != null && !sellerId.isEmpty()){
            condition.and(product.seller.id.eq(sellerId));
        }

        List<ProductWithWishDTO> content;
        if(loginUserId != null) {
            content = queryFactory
                    .select(Projections.constructor(ProductWithWishDTO.class, product, wish.id.isNotNull()))
                    .from(product)
                    .leftJoin(wish)
                    .on(wish.product.id.eq(product.id).and(wish.user.id.eq(loginUserId)))
                    .where(condition)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(QuerydslUtil.convertSort(pageable.getSort(), product))
                    .fetch();
        } else {
            content = queryFactory
                    .select(Projections.constructor(ProductWithWishDTO.class, product, Expressions.constant(false)))
                    .from(product)
                    .where(condition)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(QuerydslUtil.convertSort(pageable.getSort(), product))
                    .fetch();
        }

        @SuppressWarnings("DataFlowIssue")
        long total = queryFactory
                .select(product.count())
                .from(product)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
