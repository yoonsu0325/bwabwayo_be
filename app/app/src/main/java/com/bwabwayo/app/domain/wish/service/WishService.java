package com.bwabwayo.app.domain.wish.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.wish.domain.Wish;
import com.bwabwayo.app.domain.wish.dto.WishDTO;
import com.bwabwayo.app.domain.wish.repository.WishRepository;
import com.bwabwayo.app.global.page.PageResponseDTO;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishService {

    private final WishRepository wishRepository;
    private final ProductRepository productRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public PageResponseDTO<WishDTO> getAllMyWishes(User user, int pageNo, int pageSize) {
        // 최신순으로 정렬
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        Page<Wish> page = wishRepository.findAllByUserId(user.getId(), pageable);

        return PageResponseDTO.from(page, wish -> {
            Product product = wish.getProduct();
            Category category = product.getCategory();
            SaleStatus saleStatus = product.getSaleStatus();

            return WishDTO.builder()
                    .id(wish.getId())
                    .productId(wish.getProduct().getId())
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .thumbnail(storageService.getUrlFromKey(product.getThumbnail()))
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
        });
    }

    /**
     * 위시리스트에 상품 추가
     */
    @Transactional
    public Wish add(Product product, User user){
        // 이미 등록되어 있다면 무시
        if (existsWish(product, user)){
            log.info("이미 등록된 상품입니다. productId={}, userId={}", product.getId(), user.getId());
            return null;
        }

        // 위시 리스트에 추가
        Wish wish = Wish.builder()
                .product(product)
                .user(user)
                .build();
        wishRepository.save(wish);
        
        // 찜한 사용자의 수 갱신
        productRepository.increaseWishCount(product.getId());

        return wish;
    }

    /**
     * 위시리스트에서 상품 제거
     */
    @Transactional
    public boolean delete(Product product, User user){
        // 위시 리스트에서 제거
        long result = wishRepository.deleteByProductIdAndUserId(product.getId(), user.getId());

        // 찜한 사용자의 수 갱신
        if(result > 0){
            productRepository.decreaseWishCount(product.getId());
        }
        return result > 0;
    }

    /**
     * 사용자(userId)가 상품(productId)을 위시리스트에 등록하였는지 여부
     */
    @Transactional(readOnly = true)
    public boolean existsWish(Product productId, User userId){
        return wishRepository.existsByProductIdAndUserId(productId.getId(), userId.getId());
    }
}
