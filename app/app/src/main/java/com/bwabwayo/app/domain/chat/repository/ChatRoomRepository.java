package com.bwabwayo.app.domain.chat.repository;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findBySellerIdOrBuyerId(Long sellerId, Long buyerId);
}
