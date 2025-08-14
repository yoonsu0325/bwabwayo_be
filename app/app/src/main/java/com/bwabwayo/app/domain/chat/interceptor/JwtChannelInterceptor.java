package com.bwabwayo.app.domain.chat.interceptor;

import com.bwabwayo.app.domain.auth.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JWTUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // ✅ wrap() 말고 이걸로 가져와야, 수정 후 새 메시지 만들 수 있음
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            log.info("WS CONNECT headers={}", acc.toNativeHeaderMap());

            String auth = firstNonNull(
                    acc.getFirstNativeHeader("Authorization"),
                    acc.getFirstNativeHeader("authorization")
            );
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new MessagingException("Missing Authorization header");
            }
            String token = jwtUtils.getTokenFromHeader(auth);
            if (!jwtUtils.validateToken(token)) throw new MessagingException("Invalid token");

            String userId = jwtUtils.getSubject(token); // sub
            var principal = new UsernamePasswordAuthenticationToken(userId, null, List.of());
            acc.setUser(principal);   // ✅ Principal 세팅
            // acc.setLeaveMutable(true); // (옵션) 디버깅 시 유용
            log.info("WS CONNECT userId={}", userId);
        }

        // (옵션) 혹시 CONNECT에서 저장이 누락되면 SUBSCRIBE에서 한 번 더 보정
        if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            if (acc.getUser() == null) {
                String auth = firstNonNull(
                        acc.getFirstNativeHeader("Authorization"),
                        acc.getFirstNativeHeader("authorization")
                );
                if (auth != null && auth.startsWith("Bearer ")) {
                    String token = jwtUtils.getTokenFromHeader(auth);
                    if (jwtUtils.validateToken(token)) {
                        String userId = jwtUtils.getSubject(token);
                        acc.setUser(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
                        log.info("WS SUBSCRIBE (fallback) userId={}", userId);
                    }
                }
            }
            log.info("WS SUBSCRIBE dest={} user={}",
                    acc.getDestination(),
                    acc.getUser() != null ? acc.getUser().getName() : null);
        }

        // ✅ 핵심: 수정된 헤더로 '새 메시지'를 반환해야 Principal이 세션에 반영됨
        return MessageBuilder.createMessage(message.getPayload(), acc.getMessageHeaders());
    }
}
