package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.domain.MessageType;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.chat.dto.request.SetInvoiceNumberRequest;
import com.bwabwayo.app.domain.chat.dto.request.SetPriceRequest;
import com.bwabwayo.app.domain.chat.dto.request.SetProductStatusRequest;
import com.bwabwayo.app.domain.product.domain.Sale;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.product.service.SaleService;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemChatService {
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final ProductService productService;
    private final SaleService saleService;
    private final UserService userService;

    public void sendVideoCallMessage(ChatRoom chatRoom, String sessionId){
        MessageDTO messageDTO = MessageDTO.builder()
                .content(sessionId)
                .senderId(chatRoom.getSellerId())
                .receiverId(chatRoom.getBuyerId())
                .roomId(chatRoom.getRoomId())
                .read(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .type(MessageType.START_VIDEOCALL)
                .build();

        chatService.sendChatMessage(messageDTO);
    }

    public void sendReservationMessage(ChatRoom chatRoom, String startAt){
        MessageDTO messageDTO = MessageDTO.builder()
                .content(startAt)
                .senderId(chatRoom.getSellerId())
                .receiverId(chatRoom.getBuyerId())
                .roomId(chatRoom.getRoomId())
                .read(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .type(MessageType.RESERVE_VIDEOCALL)
                .build();

        chatService.sendChatMessage(messageDTO);
    }

    public void sendReservationCancelMessage(ChatRoom chatRoom){
        MessageDTO messageDTO = MessageDTO.builder()
                .content("")
                .senderId(chatRoom.getSellerId())
                .receiverId(chatRoom.getBuyerId())
                .roomId(chatRoom.getRoomId())
                .read(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .type(MessageType.CANCEL_VIDEOCALL)
                .build();

        chatService.sendChatMessage(messageDTO);
    }


    public void sendPaymentSuccessMessage(Long roomId){
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다"));
        MessageDTO messageDTO = MessageDTO.builder()
                .content("")
                .senderId(chatRoom.getSellerId())
                .receiverId(chatRoom.getBuyerId())
                .roomId(chatRoom.getRoomId())
                .read(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .type(MessageType.INPUT_DELIVERY_ADDRESS)
                .build();

        chatService.sendChatMessage(messageDTO);
    }

    @Transactional
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
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .type(MessageType.START_DELIVERY)
                .build();

        chatService.sendChatMessage(messageDTO);


        MessageDTO messageDTO2 = MessageDTO.builder()
                .content("")
                .senderId(chatRoom.getSellerId())
                .receiverId(chatRoom.getBuyerId())
                .roomId(roomId)
                .read(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .type(MessageType.CONFIRM_PURCHASE)
                .build();

        chatService.sendChatMessage(messageDTO2);
    }

    @Transactional
    public void setFinalPrice(Long roomId, SetPriceRequest request) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));
        Long productId = chatRoom.getProductId();
        productService.setPrice(request, productId);
        Sale sale = saleService.findByProductId(productId);
        sale.setSalePrice(request.getPrice());

        MessageDTO messageDTO = MessageDTO.builder()
                .content(request.getPrice().toString())
                .senderId(chatRoom.getSellerId())
                .receiverId(chatRoom.getBuyerId())
                .roomId(roomId)
                .read(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .type(MessageType.REQUEST_DEPOSIT)
                .build();

        chatService.sendChatMessage(messageDTO);
    }

    @Transactional
    public void setProductStatus(Long roomId, SetProductStatusRequest request) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId).orElseThrow();
        productService.setStatus(request, chatRoom.getProductId());
    }

    @Transactional
    public void startNegotiation(Long roomId, User user) throws IllegalAccessException {
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));
        if(!user.getId().equals(chatRoom.getSellerId())){
            throw new IllegalAccessException("상품의 판매자만 거래 시작을 할 수 있습니다.");
        }

        saleService.startNegotiation(user, chatRoom);

        SetProductStatusRequest productStatusRequest = SetProductStatusRequest.builder()
                .productStatus(SaleStatus.NEGOTIATING).build();

        setProductStatus(roomId, productStatusRequest);

        MessageDTO messageDTO = MessageDTO.builder()
                .content("")
                .senderId(chatRoom.getSellerId())
                .receiverId(chatRoom.getBuyerId())
                .roomId(roomId)
                .read(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .type(MessageType.START_TRADE)
                .build();

        chatService.sendChatMessage(messageDTO);
    }

    @Transactional
    public void confirmPurchase(User user, Long roomId) throws IllegalAccessException {
        //판매글 상태 변경
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));
        Long productId = chatRoom.getProductId();

        if(!(user.getId().equals(chatRoom.getBuyerId())))
            throw new IllegalAccessException("상품의 구매자만 구매확정을 할 수 있습니다.\n" + "유저 아이디: "+user.getId() +"   구매자 아이디: "+chatRoom.getBuyerId() + "   판매자 아이디: "+chatRoom.getSellerId());
        log.info("1");
        SetProductStatusRequest productStatusRequest = SetProductStatusRequest.builder()
                .productStatus(SaleStatus.SOLD_OUT).build();

        setProductStatus(roomId, productStatusRequest);

        userService.addDealCount(chatRoom.getSellerId());

        //구매 확정 시 포인트 주는 것도 추가해야함...


        //리뷰작성 메세지
        MessageDTO messageDTO = MessageDTO.builder()
                .content("")
                .senderId(chatRoom.getSellerId())
                .receiverId(chatRoom.getBuyerId())
                .roomId(roomId)
                .read(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .type(MessageType.END_TRADE)
                .build();

        chatService.sendChatMessage(messageDTO);

    }
}
