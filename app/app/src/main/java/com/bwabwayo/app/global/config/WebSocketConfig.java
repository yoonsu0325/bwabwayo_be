package com.bwabwayo.app.global.config;

import com.bwabwayo.app.domain.chat.interceptor.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub", "/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "https://localhost:3000",
                        "http://localhost:3001",
                        "https://localhost:3001",
                        "http://localhost:8081",
                        "https://i13e202.p.ssafy.io",
                        "https://i13e202.p.ssafy.io/fe/",
                        "https://i13e202.p.ssafy.io/be/",
                        "https://i13e202.p.ssafy.io/api/",
                        "https://i13e202.p.ssafy.io:3000",
                        "https://i13e202.p.ssafy.io:3001"
                ).withSockJS();
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "https://localhost:3000",
                        "http://localhost:3001",
                        "https://localhost:3001",
                        "http://localhost:8081",
                        "https://i13e202.p.ssafy.io",
                        "https://i13e202.p.ssafy.io/fe/",
                        "https://i13e202.p.ssafy.io/be/",
                        "https://i13e202.p.ssafy.io/api/",
                        "https://i13e202.p.ssafy.io:3000",
                        "https://i13e202.p.ssafy.io:3001"
                );
    }

}
