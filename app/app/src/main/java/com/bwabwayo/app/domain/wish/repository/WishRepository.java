package com.bwabwayo.app.domain.wish.repository;

import com.bwabwayo.app.domain.wish.domain.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {

    void deleteByProductIdAndUserId(Long productId, String userId);

    boolean existsByProductIdAndUserId(Long productId, String userId);

    Page<Wish> getWishesByUserId(String userId, Pageable pageable);
}
