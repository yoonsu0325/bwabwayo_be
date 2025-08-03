package com.bwabwayo.app.domain.wish.service;

import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.wish.domain.Wish;
import com.bwabwayo.app.domain.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishService {

    private final WishRepository wishRepository;


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
