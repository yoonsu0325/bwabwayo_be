package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatMessageRedisEntity;
import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.dto.request.CreateChatRoomRequest;
import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRedisRepository;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRepository;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomRedisRepository chatRoomRedisRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final RedisService redisService;

    /**
     * 채팅방 생성: MySQL 저장 + Redis 캐싱
     */
    @Transactional
    public ChatRoom createRoom(CreateChatRoomRequest request, User user) {
        // 1. DB 저장
        ChatRoom chatRoom = ChatRoom.createRoom(request, user.getId());
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        Long roomId = savedChatRoom.getRoomId();
        String sellerId = savedChatRoom.getSellerId();
        Long productId = savedChatRoom.getProductId();

        User seller = userRepository.findById(sellerId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        // 2. Redis 캐싱용 기본 미리보기 응답 생성
        ChatRoomListResponse buyerPreview = ChatRoomListResponse.fromInitial(savedChatRoom, user.getId(), seller, user, product);
        ChatRoomListResponse sellerPreview = ChatRoomListResponse.fromInitial(savedChatRoom, sellerId, seller, user, product);

        // 3. Redis에 캐싱
        chatRoomRedisRepository.setChatRoom(user.getId(), roomId, buyerPreview);
        chatRoomRedisRepository.setChatRoom(sellerId, roomId, sellerPreview);

        return savedChatRoom;
    }

    /**
     * 채팅방 목록 조회
     */
    public List<ChatRoomListResponse> getChatRoomList(String userId) {
        List<ChatRoomListResponse> roomList;

        // Redis에 존재하면 가져오기
        if (chatRoomRedisRepository.existChatRoomList(userId)) {
            roomList = chatRoomRedisRepository.getChatRoomList(userId);

            for (ChatRoomListResponse response : roomList) {
                Long roomId = response.getRoomId();

                // 마지막 메시지 갱신
                Optional<ChatMessageRedisEntity> lastMessage = redisService.findLastMessage(roomId);
                lastMessage.ifPresent(response::updateLastMessageInfo);

                // unread count 갱신
                long unreadCount = redisService.countUnreadMessages(roomId, userId);
                response.setUnreadCount(unreadCount);
            }

        } else {
            // Redis에 없으면 DB에서 조회 (예: join fetch 필요 시)
            List<ChatRoom> chatRooms = chatRoomRepository.findBySellerIdOrBuyerId(userId, userId);
            roomList = new ArrayList<>();

            for (ChatRoom chatRoom : chatRooms) {

                String buyerId = chatRoom.getBuyerId();
                String sellerId = chatRoom.getSellerId();
                Long productId = chatRoom.getProductId();

                User buyer = userRepository.findById(buyerId);
                User seller = userRepository.findById(sellerId);
                Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

                String partnerId = chatRoom.getOtherUserId(userId); // 상대 유저 ID 구하는 메서드 필요

                ChatRoomListResponse response = ChatRoomListResponse.fromInitial(chatRoom, partnerId, seller, buyer, product);
                Optional<ChatMessageRedisEntity> lastMessage = redisService.findLastMessage(chatRoom.getRoomId());

                lastMessage.ifPresent(response::updateLastMessageInfo); // ← 마지막 메시지 내용, 시간, 읽음 여부 업데이트

                Long unreadCount = redisService.countUnreadMessages(chatRoom.getRoomId(), userId);
                response.setUnreadCount(unreadCount);

                roomList.add(response);
            }

            chatRoomRedisRepository.initChatRoomList(userId, roomList); // 캐싱
        }

        return sortChatRoomListLatest(roomList);
    }

    /**
     * 마지막 메시지 기준 정렬
     */
    public List<ChatRoomListResponse> sortChatRoomListLatest(List<ChatRoomListResponse> list) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        return list.stream()
                .filter(r -> r.getLastChatmessageDto() != null)
                .sorted((o1, o2) -> {
                    try {
                        LocalDateTime t1 = LocalDateTime.parse(o1.getLastChatmessageDto().getCreatedAt(), formatter);
                        LocalDateTime t2 = LocalDateTime.parse(o2.getLastChatmessageDto().getCreatedAt(), formatter);
                        return t2.compareTo(t1); // 최신순
                    } catch (DateTimeParseException e) {
                        log.warn("⚠️ 시간 파싱 실패: {}", e.getMessage());
                        return 0;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Redis에서 채팅 메시지용 room 초기화 (선택 사항)
     */
    public void genNewRoom(Long roomId) {
        // 예: 채팅 메시지 저장용 키 초기화 등
        // redisTemplate.opsForList().leftPush("chat:" + roomId, "");
    }

    public ChatRoomListResponse getChatRoomInfo(Long roomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다: roomId=" + roomId));

        // sellerId와 buyerId를 바탕으로 상대방 정보 세팅 (예: 닉네임, 프로필 이미지 등)

        String buyerId = chatRoom.getBuyerId();
        String sellerId = chatRoom.getSellerId();
        Long productId = chatRoom.getProductId();

        User buyer = userRepository.findById(buyerId);
        User seller = userRepository.findById(sellerId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        return ChatRoomListResponse.fromInitial(chatRoom, userId, seller, buyer, product);
    }

}
