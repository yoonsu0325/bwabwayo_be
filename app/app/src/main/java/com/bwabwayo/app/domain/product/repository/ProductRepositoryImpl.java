package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.product.domain.QProduct;
import com.bwabwayo.app.domain.product.dto.ProductQueryCondition;
import com.bwabwayo.app.domain.product.dto.response.ProductWithIsLikeDTO;
import com.bwabwayo.app.domain.wish.domain.QWish;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductWithIsLikeDTO> searchByCondition(ProductQueryCondition queryCondition, Pageable pageable){
        QProduct product = QProduct.product;
        QWish wish = QWish.wish;

        BooleanBuilder whereCondition = buildWhereCondition(queryCondition);

        String viewerId = queryCondition.getViewerId();

        List<ProductWithIsLikeDTO> content;
        if(viewerId != null) {
            content = queryFactory
                    .select(Projections.constructor(ProductWithIsLikeDTO.class, product, wish.id.isNotNull()))
                    .from(product)
                    .leftJoin(wish)
                    .on(wish.user.id.eq(viewerId).and(wish.product.id.eq(product.id)))
                    .where(whereCondition)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(QuerydslUtil.convertSort(pageable.getSort(), product))
                    .fetch();
        } else {
            content = queryFactory
                    .select(Projections.constructor(ProductWithIsLikeDTO.class, product, Expressions.constant(false)))
                    .from(product)
                    .where(whereCondition)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(QuerydslUtil.convertSort(pageable.getSort(), product))
                    .fetch();
        }

        Long countResult = queryFactory
                .select(product.count())
                .from(product)
                .where(whereCondition)
                .fetchOne();

        long total = countResult != null ? countResult : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    public List<ProductWithIsLikeDTO> findByIdsInOrder(List<Long> ids, String viewerId) {
        QProduct p = QProduct.product;
        QWish w = QWish.wish;

        List<ProductWithIsLikeDTO> list;
        if(viewerId == null){
            list = queryFactory
                    .select(Projections.constructor(ProductWithIsLikeDTO.class, p, Expressions.constant(false)))
                    .from(p)
                    .where(p.id.in(ids))
                    .fetch();
        } else {
            list = queryFactory
                    .select(Projections.constructor(ProductWithIsLikeDTO.class, p, w.id.isNotNull()))
                    .from(p)
                    .leftJoin(w)
                    .on(w.user.id.eq(viewerId).and(w.product.id.eq(p.id)))
                    .where(p.id.in(ids))
                    .fetch();
        }

        Map<Long, Integer> orderMap = IntStream.range(0, ids.size())
                .boxed()
                .collect(Collectors.toMap(ids::get, Function.identity()));
        return list.stream()
                .sorted(Comparator.comparing(dto -> orderMap.getOrDefault(dto.getProduct().getId(), Integer.MAX_VALUE)))
                .collect(Collectors.toList());
    }

    private BooleanBuilder buildWhereCondition(ProductQueryCondition queryCondition) {
        QProduct product = QProduct.product;

        BooleanBuilder whereCondition = new BooleanBuilder();

        // 키워드 검색
        String keyword = queryCondition.getKeyword();
        if (keyword != null && !keyword.isEmpty()) {
            whereCondition.and(product.title.containsIgnoreCase(keyword));
        }

        // 카테고리 필터링
        List<Long> categoryIn = queryCondition.getCategoryIn();
        if (categoryIn != null && !categoryIn.isEmpty()) {
            whereCondition.and(product.category.id.in(categoryIn));
        }

        // 판매자 필터링
        String sellerId = queryCondition.getSellerId();
        if(sellerId != null && !sellerId.isEmpty()){
            whereCondition.and(product.seller.id.eq(sellerId));
        }

        // 거래 조건 필터링
        // 화상 통화 가능 여부
        Boolean canVideoCall = queryCondition.getCanVideoCall();
        if(canVideoCall != null){
            whereCondition.and(product.canVideoCall.eq(canVideoCall));
        }
        // 가격 협상 가능 여부
        Boolean canNegotiate = queryCondition.getCanNegotiate();
        if(canNegotiate != null){
            whereCondition.and(product.canNegotiate.eq(canNegotiate));
        }
        // 택배거래 가능 여부
        Boolean canDelivery = queryCondition.getCanDelivery();
        if(canDelivery != null){
            whereCondition.and(product.canDelivery.eq(canDelivery));
        }
        // 직거래 가능 여부
        Boolean canDirect = queryCondition.getCanDirect();
        if(canDirect!=null){
            whereCondition.and(product.canDirect.eq(canDirect));
        }

        // 가격 필터링
        Integer minPrice = queryCondition.getMinPrice();
        Integer maxPrice = queryCondition.getMaxPrice();

        if(minPrice != null && maxPrice != null){
            whereCondition.and(product.price.between(minPrice, maxPrice));
        } else if(minPrice != null){
            whereCondition.and(product.price.goe(minPrice));
        } else if(maxPrice != null){
            whereCondition.and(product.price.loe(maxPrice));
        }

        return whereCondition;
    }

    public long getCount(ProductQueryCondition condition){
        QProduct product = QProduct.product;

        BooleanBuilder whereCondition = buildWhereCondition(condition);

        Long countResult = queryFactory
                .select(product.count())
                .from(product)
                .where(whereCondition)
                .fetchOne();

        return countResult != null ? countResult : 0L;
    }
}
