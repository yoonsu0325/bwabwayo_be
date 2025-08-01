package com.bwabwayo.app.domain.chat.controller;

import com.bwabwayo.app.domain.chat.dto.request.ReservationRequest;
import com.bwabwayo.app.domain.chat.service.ReservationService;
import com.bwabwayo.app.domain.user.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms/{roomId}/schedule")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<?> makeReservation(
            @PathVariable Long roomId,
            @LoginUser User user,
            @RequestBody ReservationRequest reservationRequest) throws IllegalAccessException {
        return ResponseEntity.ok(
                reservationService.makeReservation(user, reservationRequest));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> cancelReservation(
            @PathVariable Long roomId,
            @PathVariable Long scheduleId,
            @LoginUser User user){
        reservationService.cancelReservation(roomId, scheduleId, user);
        return ResponseEntity.ok("예약이 취소되었습니다");
    }


}
