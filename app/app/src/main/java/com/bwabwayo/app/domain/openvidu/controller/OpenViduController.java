package com.bwabwayo.app.domain.openvidu.controller;

import io.openvidu.java.client.ConnectionProperties;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.Session;
import io.openvidu.java.client.SessionProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class OpenViduController {

    private final OpenVidu openVidu;

    public OpenViduController(OpenVidu openVidu) {
        this.openVidu = openVidu;
    }


    // 세션 발급
    @PostMapping
    public ResponseEntity<String> initializeSession(@RequestBody Map<String, Object> params) throws Exception {
        SessionProperties props = SessionProperties.fromJson(params).build();
        Session session = openVidu.createSession(props);
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
}
