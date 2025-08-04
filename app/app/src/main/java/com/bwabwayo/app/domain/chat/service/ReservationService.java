package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.chat.domain.VideocallReservation;
import com.bwabwayo.app.domain.chat.dto.request.ReservationRequest;
import com.bwabwayo.app.domain.chat.dto.response.ReservationResponse;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRepository;
import com.bwabwayo.app.domain.chat.repository.ReservationRepository;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.user.domain.PointEventType;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.service.UserService;
import com.bwabwayo.app.global.common.CommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final CommonService commonService;
    private final Integer RESERVATION_POINT = 1000;

    public VideocallReservation makeReservation(User user, ReservationRequest reservationRequest, Long roomId) throws IllegalAccessException {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

        if(!chatRoom.getBuyerId().equals(user.getId()))
            throw new IllegalAccessException("접근할 수 없는 채팅방입니다");

        // 포인트 소비
        userService.calcPoint(PointEventType.VIDEO_CALL, -1 * RESERVATION_POINT, user);
        return reservationRepository.save(
                VideocallReservation.of(reservationRequest, user.getId(), chatRoom.getSellerId(), roomId));
    }

    public void cancelReservation(Long roomId, Long scheduleId, User user) throws IllegalAccessException {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

        // 예약 취소
        // 화상 시작 전 12시간 내에 취소를 하면 패널티

        //채팅방 - 유저 확인
        if(!chatRoom.getSellerId().equals(user.getId()) && !chatRoom.getBuyerId().equals(user.getId()))
            throw new IllegalAccessException("접근할 수 없는 채팅방입니다");

        VideocallReservation reservation = reservationRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));

        //채팅방 - 예약 확인
        if(!reservation.getRoomId().equals(roomId)){
            throw new IllegalAccessException("접근할 수 없는 채팅방입니다");
        }
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime reservedTime = commonService.parseSafe(reservation.getStartAt());
        LocalDateTime noPenaltyTime = reservedTime.minusHours(12);
        User buyer = userService.findById(reservation.getBuyerId());

        if(noPenaltyTime.compareTo(now) > 0){
            //패널티 없음
            userService.calcPoint(PointEventType.VIDEO_CALL, RESERVATION_POINT, buyer);
        }
        else {
            //패널티
            //판매자가 취소 -> 판매자 패널티 + 구매자 포인트 돌려받음
            if(user.getId().equals(reservation.getSellerId())){
                User seller = userService.findById(reservation.getSellerId());
                //userService.panalize(seller);
                userService.calcPoint(PointEventType.VIDEO_CALL, RESERVATION_POINT, buyer);
            }
            //구매자가 취소 -> 그냥 취소
        }
        reservationRepository.delete(reservation);
    }

    public List<ReservationResponse> findAllReservations(User user){
        List<VideocallReservation> reservationToBuy = reservationRepository.findAllByBuyerId(user.getId());
        List<VideocallReservation> reservationToSell = reservationRepository.findAllBySellerId(user.getId());

        List<ReservationResponse> reservationResponseList = new ArrayList<>();
        for (VideocallReservation reservation : reservationToBuy) {
            String sellerId = reservation.getSellerId();
            User seller = userService.findById(sellerId);
            ChatRoom chatRoom = chatRoomRepository.findByRoomId(reservation.getRoomId()).orElseThrow();
            Product product = productRepository.findById(chatRoom.getProductId()).orElseThrow();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservedTime = commonService.parseSafe(reservation.getStartAt());

            User buyer = userService.findById(reservation.getBuyerId());

            boolean isEnd = true;
            String replayUrl = null;
            if(reservedTime.compareTo(now) > 0){
                //예약시간이 현재보다 이후
                //아직 화상거래 안함
                isEnd = false;
            }
            else{
                //화상거래함

            }

            reservationResponseList.add(ReservationResponse.from(reservation, seller.getNickname(), product.getTitle(), Optional.ofNullable(replayUrl), isEnd));
        }

        for (VideocallReservation reservation : reservationToSell) {
            String buyerId = reservation.getBuyerId();
            User buyer = userService.findById(buyerId);
            ChatRoom chatRoom = chatRoomRepository.findByRoomId(reservation.getRoomId()).orElseThrow();
            Product product = productRepository.findById(chatRoom.getProductId()).orElseThrow();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservedTime = commonService.parseSafe(reservation.getStartAt());

            User seller = userService.findById(reservation.getSellerId());

            boolean isEnd = true;
            String replayUrl = null;
            if(reservedTime.compareTo(now) > 0){
                //예약시간이 현재보다 이후
                //아직 화상거래 안함
                isEnd = false;
            }
            else{
                //화상거래함

            }

            reservationResponseList.add(ReservationResponse.from(reservation, seller.getNickname(), product.getTitle(), Optional.ofNullable(replayUrl), isEnd));
        }
        return reservationResponseList;
    }


}
