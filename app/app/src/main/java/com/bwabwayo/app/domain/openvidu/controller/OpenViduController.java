package com.bwabwayo.app.domain.openvidu.controller;

import com.bwabwayo.app.domain.chat.repository.ReservationRepository;
import com.bwabwayo.app.domain.openvidu.dto.request.SessionRequest;
import com.bwabwayo.app.global.storage.service.S3Service;
import io.openvidu.java.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sessions")
public class OpenViduController {

    // 오픈비두 객체
    private final OpenVidu openVidu;

    // 스케줄러 , 화상채팅 자동 종료
    private final TaskScheduler taskScheduler;

    private final ReservationRepository reservationRepository;

    // S3
    private final S3Service s3Service;

     // 녹화 속성 개체
    private final RecordingProperties recProps = new RecordingProperties.Builder()
            .outputMode(Recording.OutputMode.COMPOSED)      // 스트림 합성 모드
            .recordingLayout(RecordingLayout.BEST_FIT)      // 화면 레이아웃
            .resolution("1280x720")                         // 해상도
            .hasAudio(true)                                 // 오디오 녹음
            .hasVideo(true)                                 // 비디오 녹화
            .build();

    @Autowired
    public OpenViduController(OpenVidu openVidu, TaskScheduler taskScheduler,
                              ReservationRepository reservationRepository,
                              S3Service s3Service) {
        this.openVidu = openVidu;
        this.taskScheduler = taskScheduler;
        this.reservationRepository = reservationRepository;
        this.s3Service = s3Service;
    }


    // 세선 생성( 화상채팅 오픈, 자동 녹화, 30분뒤 자동 종료)
    @PostMapping
    public ResponseEntity<String> initializeSession(@RequestBody SessionRequest sessionRequest) throws Exception {

        // 세션 생성, 녹화 옵션
        String customId = String.valueOf(sessionRequest.getVideoRoomId());

        SessionProperties props = new SessionProperties.Builder()
                .customSessionId(customId)
                .recordingMode(RecordingMode.ALWAYS)
                .defaultRecordingProperties(recProps)
                .build();

        Session session = openVidu.createSession(props);
        String sessionId = session.getSessionId();

        taskScheduler.schedule(() -> handleRecordingComplete(sessionId),
                new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)));

        return new ResponseEntity<>(session.getSessionId(), HttpStatus.OK);
    }

    // 토큰
    @PostMapping("/{sessionId}/token")
    public ResponseEntity<String> createToken(@PathVariable("sessionId") String sessionId,
                                              @RequestBody Map<String, Object> params) throws Exception {
        Session session = openVidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ConnectionProperties connProps = ConnectionProperties.fromJson(params).build();
        String token = session.createConnection(connProps).getToken();
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    private void handleRecordingComplete(String sessionId) {
        try {
            Session active = openVidu.getActiveSession(sessionId);

            // 🔐 세션이 살아있고, 참여자가 1명 이상일 때만 종료
            openVidu.fetch();

            // ✅ 세션이 메모리에 존재할 때만 처리
            if (active != null) {
                try {
                    // ✅ 녹화 중이면 녹화 종료
                    if (!active.getConnections().isEmpty()) {
                        openVidu.stopRecording(sessionId);
                    }

                    // ✅ 세션 강제 종료 시도
                    active.close();

                } catch (OpenViduHttpException e) {
                    if (e.getStatus() == 404) {
                        System.out.println("[INFO] 세션이 이미 종료되어 있음: " + sessionId);
                    } else {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("[INFO] 세션이 이미 사라짐: " + sessionId);
            }


            // 다시보기 녹화 url
            String url = String.format(
                    "https://i13e202.p.ssafy.io/recordings/%s/%s.mp4",
                    sessionId, sessionId);
            // 다시보기 녹화 url 있는지 확인후 있으면 객체에 저장
            if (waitForUrl(url, 10, 1000)) {
                Long id = Long.valueOf(sessionId);
                reservationRepository.findById(id).ifPresent(res -> {
                    res.setVideoCallUrl(url);
                    reservationRepository.save(res);
                });
                s3Service.upload(url, "video");
            } else {
                System.err.println("녹화 파일이 끝까지 생성되지 않았습니다: " + url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean urlExists(String urlString) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(urlString).openConnection();
            con.setRequestMethod("HEAD");
            con.setConnectTimeout(3000);
            con.connect();
            int code = con.getResponseCode();
            return (code == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean waitForUrl(String url, int maxAttempts, long delayMillis) {
        for (int i = 0; i < maxAttempts; i++) {
            if (urlExists(url)) {
                return true;
            }
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 인터럽트 처리
                return false;
            }
        }
        return false;
    }

}