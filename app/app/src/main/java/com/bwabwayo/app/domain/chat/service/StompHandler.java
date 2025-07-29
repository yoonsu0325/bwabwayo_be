package com.bwabwayo.app.domain.chat.service;
import com.bwabwayo.app.domain.chat.repository.ChatRoomRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final ChatRoomRedisRepository chatRoomRedisRepository;
    private final ChatService chatService;

    /** websocket을 통해 들어온 요청이 처리 되기전 실행된다.*/
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE == accessor.getCommand()) { // 채팅룸 구독요청

            String sessionId = (String) message.getHeaders().get("simpSessionId");
            log.info("Websocket CONNECT simpSessionId : {}", sessionId);


        } else if (StompCommand.DISCONNECT == accessor.getCommand()) { // Websocket 연결 종료

        }

        return message;
    }
}

