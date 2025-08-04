package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.domain.MessageType;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.chat.dto.request.SetInvoiceNumberRequest;
import com.bwabwayo.app.domain.chat.dto.request.SetPriceRequest;
import com.bwabwayo.app.domain.chat.dto.request.SetProductStatusRequest;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.global.common.CommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemChatService {
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final ProductService productService;
    private final CommonService commonService;
    public void setInvoiceNumber(Long roomId, SetInvoiceNumberRequest request) {

        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));
        Long productId = chatRoom.getProductId();
        productService.setInvoiceNumber(request, productId);

        MessageDTO messageDTO = MessageDTO.builder()
                .content("")
                .senderId(chatRoom.getSellerId())
                .receiverId(chatRoom.getBuyerId())
                .roomId(roomId)
                .read(false)
                .createdAt(LocalDateTime.now().toString())
                .type(MessageType.CONFIRM_PURCHASE)
                .build();

        chatService.sendChatMessage(messageDTO);
    }

    public void setFinalPrice(Long roomId, SetPriceRequest request) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));
        Long productId = chatRoom.getProductId();
        productService.setPrice(request, productId);

        MessageDTO messageDTO = MessageDTO.builder()
                .content("")
                .senderId(chatRoom.getSellerId())
                .receiverId(chatRoom.getBuyerId())
                .roomId(roomId)
                .read(false)
                .createdAt(LocalDateTime.now().toString())
                .type(MessageType.START_TRADE)
                .build();

        chatService.sendChatMessage(messageDTO);
    }

    public void setProductStatus(Long roomId, SetProductStatusRequest request) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));
        Long productId = chatRoom.getProductId();
        productService.setStatus(request, productId);
    }
}
