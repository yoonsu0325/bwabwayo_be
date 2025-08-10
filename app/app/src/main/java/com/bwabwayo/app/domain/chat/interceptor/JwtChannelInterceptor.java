package com.bwabwayo.app.domain.chat.interceptor;

import com.bwabwayo.app.domain.auth.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JWTUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            // 1) Authorization 헤더 추출
            String auth = acc.getFirstNativeHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                log.warn("STOMP CONNECT without Authorization header");
                throw new IllegalArgumentException("Missing Authorization header");
            }

            // 2) 토큰 파싱
            String token = jwtUtils.getTokenFromHeader(auth);

            // 3) 유효성 검사
            if (!jwtUtils.validateToken(token)) {
                log.warn("Invalid JWT token on STOMP CONNECT");
                throw new IllegalArgumentException("Invalid token");
            }

            // (선택) Access 토큰만 허용
            String tokenType = jwtUtils.getTokenType(token);
            if (tokenType == null || !"ACCESS".equalsIgnoreCase(tokenType)) {
                log.warn("Non-access token on STOMP CONNECT: {}", tokenType);
                throw new IllegalArgumentException("Access token required");
            }

            // 4) subject(id) 추출해서 Principal 설정
            String userId = jwtUtils.getSubject(token); // sub = userId
            if (userId == null || userId.isBlank()) {
                log.warn("No subject in JWT token");
                throw new IllegalArgumentException("No subject in token");
            }

            UsernamePasswordAuthenticationToken principal =
                    new UsernamePasswordAuthenticationToken(userId, null, java.util.List.of());

            acc.setUser(principal); // ★ 이후 convertAndSendToUser(userId, ...)에서 이 userId 사용
            log.debug("STOMP CONNECT authenticated userId={}", userId);
        }

        return message;
    }
}
