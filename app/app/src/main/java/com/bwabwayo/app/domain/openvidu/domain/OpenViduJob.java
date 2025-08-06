package com.bwabwayo.app.domain.openvidu.domain;

import com.bwabwayo.app.domain.chat.domain.VideocallReservation;
import com.bwabwayo.app.domain.chat.repository.ReservationRepository;
import com.bwabwayo.app.domain.openvidu.service.OpenviduService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@RequiredArgsConstructor
public class OpenViduJob implements Job {

    private final OpenviduService openviduService;
    private final ReservationRepository reservationRepository;

    @Override
    public void execute(JobExecutionContext ctx) {
        Long reservationId = ctx.getJobDetail().getJobDataMap().getLong("reservationId");
        VideocallReservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));
        try {
            String sessionId = openviduService.initializeSession(reservationId);
            reservation.setSessionId(sessionId);
            reservationRepository.save(reservation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
