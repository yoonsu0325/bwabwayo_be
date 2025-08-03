package com.bwabwayo.app.domain.wish.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.wish.domain.Wish;
import com.bwabwayo.app.domain.wish.dto.response.WishDTO;
import com.bwabwayo.app.domain.wish.dto.response.WishlistResponseDTO;
import com.bwabwayo.app.domain.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishService {

    private final WishRepository wishRepository;

    @Transactional(readOnly = true)
    public WishlistResponseDTO getAllMyWishes(User user, int pageNo, int pageSize) {
        if(user == null || user.getId() == null) throw new IllegalArgumentException("유저가 존재하지 않습니다.");

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Wish> pageWish = wishRepository.getWishesByUserId(user.getId(), pageable);

        List<Wish> content = pageWish.getContent();

        int current = pageWish.getNumber() + 1;
        int end = (int)Math.ceil(current / 10.0) * 10;
        int start = end - 9;
        int last =  Math.max(current, Math.min(end, pageWish.getTotalPages()));
        boolean prev = current > 1;
        boolean next =  pageWish.hasNext();
        int totalPages = pageWish.getTotalPages();
        long totalItems = pageWish.getTotalElements();

        List<WishDTO> wishDTOs = content.stream()
                .map(wish-> {
                    Product product = wish.getProduct();
                    Category category = product.getCategory();
                    SaleStatus saleStatus = product.getSaleStatus();

                    return WishDTO.builder()
                            .id(wish.getId())
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .thumbnail(product.getThumbnail())
                            .title(product.getTitle())
                            .price(product.getPrice())
                            .viewCount(product.getViewCount())
                            .wishCount(product.getWishCount())
                            .chatCount(product.getChatCount())
                            .canVideoCall(product.isCanVideoCall())
                            .saleStatusCode(saleStatus.getLevel())
                            .saleStatus(saleStatus.getDescription())
                            .createdAt(product.getCreatedAt())
                            .build();
                }).toList();


        return WishlistResponseDTO.builder()
                .wishes(wishDTOs)
                .current(current)
                .start(start)
                .last(last)
                .prev(prev)
                .next(next)
                .totalPages(totalPages)
                .totalItems(totalItems)
                .build();
    }


    @Transactional
    public void addWish(Product product, User user){
        if(product == null || product.getId() == null) throw new IllegalArgumentException("상품이 존재하지 않습니다.");
        if(user == null || user.getId() == null) throw new IllegalArgumentException("유저가 존재하지 않습니다.");
        
        // 이미 등록되어 있다면 무시
        if (existsWish(product, user)){
            log.info("이미 등록된 상품입니다. productId={}, userId={}", product.getId(), user.getId());
            return;
        }

        Wish wish = Wish.builder()
                .product(product)
                .user(user)
                .build();
        wishRepository.save(wish);
    }

    @Transactional
    public void removeWish(Product product, User user){
        if(product == null || product.getId() == null) throw new IllegalArgumentException("상품이 존재하지 않습니다.");
        if(user == null || user.getId() == null) throw new IllegalArgumentException("유저가 존재하지 않습니다.");

        wishRepository.deleteByProductIdAndUserId(product.getId(), user.getId());
    }

    @Transactional(readOnly = true)
    public boolean existsWish(Product product, User user){
        if(product == null || product.getId() == null) throw new IllegalArgumentException("상품이 존재하지 않습니다.");
        if(user == null || user.getId() == null) throw new IllegalArgumentException("유저가 존재하지 않습니다.");

        return wishRepository.existsByProductIdAndUserId(product.getId(), user.getId());
    }
}
