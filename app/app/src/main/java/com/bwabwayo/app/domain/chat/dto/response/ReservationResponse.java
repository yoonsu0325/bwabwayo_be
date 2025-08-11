package com.bwabwayo.app.domain.chat.dto.response;

import com.bwabwayo.app.domain.chat.domain.VideocallReservation;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class ReservationResponse {
    private Long roomId;
    private String partnerNickname;
    private String title;
    private String startAt;
    private String replayUrl;
    private Long scheduleId;
    private boolean isEnd;

    public static ReservationResponse from(VideocallReservation reservation, String partnerNickname,
                                           String title, Optional<String> replayUrl, boolean isEnd){
        String replay = null;
        if(replayUrl.isPresent()) replay = replayUrl.get();
        return ReservationResponse.builder()
                .roomId(reservation.getRoomId())
                .partnerNickname(partnerNickname)
                .title(title)
                .startAt(reservation.getStartAt())
                .replayUrl(replay)
                .scheduleId(reservation.getId())
                .isEnd(isEnd)
                .build();
    }
}
