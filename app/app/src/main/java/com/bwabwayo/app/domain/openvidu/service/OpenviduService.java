package com.bwabwayo.app.domain.openvidu.service;


import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor
public class OpenviduService {

    // 오픈비두 객체
    private final OpenVidu openVidu;

    // 스케줄러 , 화상채팅 자동 종료
    private final TaskScheduler taskScheduler;

    // 녹화 속성 개체
    private final RecordingProperties recProps = new RecordingProperties.Builder()
            .outputMode(Recording.OutputMode.COMPOSED)      // 스트림 합성 모드
            .recordingLayout(RecordingLayout.BEST_FIT)      // 화면 레이아웃
            .resolution("1280x720")                         // 해상도
            .hasAudio(true)                                 // 오디오 녹음
            .hasVideo(true)                                 // 비디오 녹화
            .build();

    // 세선 생성( 화상채팅 오픈, 자동 녹화, 30분뒤 자동 종료)
    public String initializeSession(Long reservationId) throws Exception {

        // 세션 생성, 녹화 옵션
        String customId = String.valueOf(reservationId);

        SessionProperties props = new SessionProperties.Builder()
                .customSessionId(customId)
                .recordingMode(RecordingMode.ALWAYS)
                .defaultRecordingProperties(recProps)
                .build();

        Session session = openVidu.createSession(props);
        String sessionId = session.getSessionId();


        // 30분 뒤에 실행될 Date 객체 생성
        Date runAt = new Date(System.currentTimeMillis()
                + TimeUnit.MINUTES.toMillis(30));

        // 30분 뒤에 세션 강제 종료 스케줄링
        taskScheduler.schedule(() -> {
            try {
                Session active = openVidu.getActiveSession(sessionId);
                if (active != null) {
                    active.close();
                    System.out.println("Session " + sessionId + " closed after 30 minutes");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, runAt);

        return session.getSessionId();
    }

}
