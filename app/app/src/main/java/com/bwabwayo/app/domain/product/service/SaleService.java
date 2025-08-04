package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.domain.Sale;
import com.bwabwayo.app.domain.product.repository.SaleRepository;
import com.bwabwayo.app.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductService productService;
    public Sale startNegotiation(User user, ChatRoom chatRoom) {
        Product product = productService.findById(chatRoom.getProductId());
        Sale sale = Sale.builder()
                .productId(chatRoom.getProductId())
                .sellerId(chatRoom.getSellerId())
                .buyerId(chatRoom.getBuyerId())
                .salePrice(product.getPrice())
                .createdAt(LocalDateTime.now().toString())
                .isReviewed(false)
                .roomId(chatRoom.getRoomId())
                .build();

        return saleRepository.save(sale);
    }
}
