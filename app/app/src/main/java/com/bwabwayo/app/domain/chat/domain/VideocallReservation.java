package com.bwabwayo.app.domain.chat.domain;

import com.bwabwayo.app.domain.chat.dto.request.ReservationRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideocallReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String buyerId;
    private String sellerId;
    private Long roomId;
    private String startAt;
    private String sessionId;

    public static VideocallReservation of(ReservationRequest reservation, String buyerId, String sellerId, Long roomId){
        return VideocallReservation.builder()
                .buyerId(buyerId)
                .sellerId(sellerId)
                .roomId(roomId)
                .startAt(reservation.getStartAt())
                .build();
    }
}
