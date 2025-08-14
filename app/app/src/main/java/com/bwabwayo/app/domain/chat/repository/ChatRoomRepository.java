package com.bwabwayo.app.domain.chat.repository;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findBySellerIdOrBuyerId(String sellerId, String buyerId);

    Optional<ChatRoom> findByProductIdAndSellerIdAndBuyerId(Long productId, String sellerId, String id);

    Optional<ChatRoom> findByRoomId(Long roomId);
}
