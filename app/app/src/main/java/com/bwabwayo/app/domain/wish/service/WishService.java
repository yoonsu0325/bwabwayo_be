package com.bwabwayo.app.domain.wish.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.wish.domain.Wish;
import com.bwabwayo.app.domain.wish.dto.response.WishDTO;
import com.bwabwayo.app.domain.wish.repository.WishRepository;
import com.bwabwayo.app.global.page.PageResponseDTO;
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

    @Transactional(readOnly = true)
    public PageResponseDTO<WishDTO> getAllMyWishes(User user, int pageNo, int pageSize) {
        if(user == null || user.getId() == null) throw new IllegalArgumentException("유저가 존재하지 않습니다.");
        
        // 최신순으로 정렬
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Wish> page = wishRepository.getWishesByUserId(user.getId(), pageable);

        return PageResponseDTO.fromEntity(page, wish -> {
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
        });
    }

    /**
     * 위시리스트에 상품 추가
     */
    @Transactional
    public Wish addWish(Product product, User user){
        if(product == null || product.getId() == null) throw new IllegalArgumentException("상품이 존재하지 않습니다.");
        if(user == null || user.getId() == null) throw new IllegalArgumentException("유저가 존재하지 않습니다.");
        
        // 이미 등록되어 있다면 무시
        if (existsWish(product.getId(), user.getId())){
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
    public boolean removeWish(Long productId, String userId){
        // 위시 리스트에서 제거
        long result = wishRepository.deleteByProductIdAndUserId(productId, userId);
        if(result > 0){
            // 찜한 사용자의 수 갱신
            productRepository.decreaseWishCount(productId);
        }
        return result > 0;
    }

    @Transactional(readOnly = true)
    public boolean existsWish(Long productId, String userId){
        return wishRepository.existsByProductIdAndUserId(productId, userId);
    }
}
