package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatMessageRedisEntity;
import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.dto.request.CreateChatRoomRequest;
import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
import com.bwabwayo.app.domain.chat.dto.response.ProductInfoResponse;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRedisRepository;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRepository;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.user.domain.ReviewAgg;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.service.UserService;
import com.bwabwayo.app.global.common.CommonService;
import com.bwabwayo.app.global.storage.service.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomRedisRepository chatRoomRedisRepository;
    private final UserService userService;
    private final ProductService productService;
    private final RedisService redisService;
    private final CommonService commonService;
    private final StorageService storageService;
    private final ProductRepository productRepository;

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

        User seller = userService.findById(sellerId);
        Product product = productService.findById(productId);

        ReviewAgg reviewAgg = userService.findReviewAggByUser(sellerId);
        String url = storageService.getUrlFromKey(product.getThumbnail());

        String sellerImage = storageService.getUrlFromKey(seller.getProfileImage());
        String buyerImage = storageService.getUrlFromKey(user.getProfileImage());

        // 2. Redis 캐싱용 기본 미리보기 응답 생성
        ChatRoomListResponse buyerPreview = ChatRoomListResponse
                .fromInitial(savedChatRoom, user.getId(), seller, reviewAgg, user, product, url,
                        sellerImage, buyerImage);
        ChatRoomListResponse sellerPreview = ChatRoomListResponse
                .fromInitial(savedChatRoom, sellerId, seller, reviewAgg, user, product, url,
                        sellerImage, buyerImage);

        // 3. Redis에 캐싱
        chatRoomRedisRepository.setChatRoom(user.getId(), roomId, buyerPreview);
        chatRoomRedisRepository.setChatRoom(sellerId, roomId, sellerPreview);

        product.addChatCount();

        return savedChatRoom;
    }

    /**
     * 채팅방 목록 조회
     */
    public List<ChatRoomListResponse> getChatRoomList(String userId) {
        List<ChatRoomListResponse> roomList;

        // 0) saleStatus 최신화에 필요한 productId를 수집
        Set<Long> productIds = new HashSet<>();

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

                // productId 수집 (null 가드)
                if (response.getProduct() != null && response.getProduct().getId() != null) {
                    productIds.add(response.getProduct().getId());
                }
            }
            // 1) RDB에서 최신 product 상태 일괄 조회
            if (!productIds.isEmpty()) {
                List<Product> products = productRepository.findAllById(productIds);
                Map<Long, SaleStatus> statusById = products.stream()
                        .collect(Collectors.toMap(Product::getId, Product::getSaleStatus));

                // 2) 각 response의 product.saleStatus를 최신값으로 덮어쓰기
                for (ChatRoomListResponse response : roomList) {
                    ProductInfoResponse p = response.getProduct();
                    if (p == null || p.getId() == null) continue;

                    SaleStatus latest = statusById.get(p.getId());
                    if (latest != null) {
                        p.setSaleStatus(latest); // ProductInfoResponse에 @Setter 있으니 가능
                    }
                    // latest가 없으면(삭제/비활성화 등) 기존 Redis 값 유지
                }
            }

        } else {
            // Redis에 없으면 DB에서 조회 (예: join fetch 필요 시)
            List<ChatRoom> chatRooms = chatRoomRepository.findBySellerIdOrBuyerId(userId, userId);
            roomList = new ArrayList<>();

            for (ChatRoom chatRoom : chatRooms) {

                String buyerId = chatRoom.getBuyerId();
                String sellerId = chatRoom.getSellerId();
                Long productId = chatRoom.getProductId();

                User buyer = userService.findById(buyerId);
                User seller = userService.findById(sellerId);

                String sellerImage = storageService.getUrlFromKey(seller.getProfileImage());
                String buyerImage = storageService.getUrlFromKey(buyer.getProfileImage());

                Product product = productService.findById(productId);

                String partnerId = chatRoom.getOtherUserId(userId); // 상대 유저 ID 구하는 메서드 필요

                ReviewAgg reviewAgg = userService.findReviewAggByUser(sellerId);
                String url = storageService.getUrlFromKey(product.getThumbnail());

                ChatRoomListResponse response = ChatRoomListResponse
                        .fromInitial(chatRoom, partnerId, seller, reviewAgg, buyer, product, url,
                                sellerImage, buyerImage);
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

        return list.stream()
                .filter(r -> productRepository.existsById(r.getProduct().getId())) // N+1이라서 수정 필요
                .filter(r -> r.getLastMessage() != null)
                .sorted((o1, o2) -> {
                    try {
                        LocalDateTime t1 = commonService.parseSafe(o1.getLastMessage().getCreatedAt());
                        LocalDateTime t2 = commonService.parseSafe(o2.getLastMessage().getCreatedAt());
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

        User buyer = userService.findById(buyerId);
        User seller = userService.findById(sellerId);

        String sellerImage = storageService.getUrlFromKey(seller.getProfileImage());
        String buyerImage = storageService.getUrlFromKey(buyer.getProfileImage());


        Product product = productService.findById(productId);

        ReviewAgg reviewAgg = userService.findReviewAggByUser(sellerId);
        String url = storageService.getUrlFromKey(product.getThumbnail());

        return ChatRoomListResponse
                .fromInitial(chatRoom, userId, seller, reviewAgg, buyer, product, url,
                        sellerImage, buyerImage);
    }


    public Optional<ChatRoom> find(CreateChatRoomRequest request, User user) {
        return chatRoomRepository.findByProductIdAndSellerIdAndBuyerId(
                request.getProductId(), request.getSellerId(), user.getId());
    }

    public Optional<ChatRoom> findByRoomId(Long roomId) {
        return chatRoomRepository.findByRoomId(roomId);
    }
}
