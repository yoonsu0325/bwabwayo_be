package com.bwabwayo.app.global.config;

import com.bwabwayo.app.domain.chat.domain.ChatMessageRedisEntity;
import com.bwabwayo.app.domain.chat.dto.response.ChatRoomListResponse;
import com.bwabwayo.app.domain.chat.service.RedisSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;


    // Redis 연결 팩토리 (연결 설정)
    private RedisConnectionFactory createFactory(int dbIndex) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setPassword(redisPassword);
        config.setDatabase(dbIndex);
        return new LettuceConnectionFactory(config);
    }

    @Primary
    @Bean
    public RedisConnectionFactory redisConnectionFactory0() {
        return createFactory(0);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory1() {
        return createFactory(1);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory2() {
        return createFactory(2);
    }


    // ObjectMapper (직렬화 기본 설정)
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    // RedisTemplate (기본 직렬화 설정, DB0 사용)
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // Redis의 key-value 저장을 처리하는 핵심 도구 (String key, Object value)
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // RedisConnectionFactory : Redis 서버와의 연결을 관리하는 객체
        // RedisTemplate 객체 생성 후 Redis 연결 팩토리 주입 (redis 연결할 때 사용)
        template.setConnectionFactory(redisConnectionFactory);

        // key를 byte배열(byte[]) 형태로 바꿔주는 설정
        // Key값을 어덯게 직렬화 할 것인지 설정하는 메서드
        //StringRedisSerializer : String -> byte[]로 직렬화해서 사람이 읽을 수 있는 문자열로 변경 (역직렬화도 가능)
        template.setKeySerializer(new StringRedisSerializer());
        // value가 Hash일 때, Hash의 key값을 어떻게 직렬화 할 것인지 설정하는 메서드
        template.setHashKeySerializer(new StringRedisSerializer());

        // value를 byte배열(byte[]) 형태로 바꿔주는 설정 (Java 객체 -> byte[])
        ObjectMapper mapper = objectMapper(); //LocalDateTime 타입 처리를 위한 함수 사용
        //Jackson2JsonRedisSerializer는 Java 객체를 JSON 문자열로 바꾼 뒤 byte[]로 변환해 저장할 때 사용할 직렬화기 생성
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // 직렬화기에 objectMapper()도 설정
        serializer.setObjectMapper(mapper);

        // opsForValue() 등에서 쓰는 단일 key-value 형태의 값 직렬화 방식
        template.setValueSerializer(serializer);
        //opsForHash()에서 Hash의 value (필드의 값) 직렬화 방식
        template.setHashValueSerializer(serializer);

        // afterPropertiesSet()을 호출하면 내부적으로 설정한 직렬화기, 커넥션 팩토리 등을 기반으로 초기화 작업
        template.afterPropertiesSet();
        return template;
    }


    /** 조회수 */
    @Bean
    public RedisTemplate<String, Long> redisViewCountTemplate(@Qualifier("redisConnectionFactory2") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        template.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));

        template.afterPropertiesSet();
        return template;
    }

    /** 뷰어 */
    @Bean
    public RedisTemplate<String, String> redisViewerTemplate(@Qualifier("redisConnectionFactory2") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    // RefreshTokenTemplate (DB1 사용)
    @Bean
    @Primary
    public RedisTemplate<String, String> redisRefreshTokenTemplate(@Qualifier("redisConnectionFactory1") RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        template.setConnectionFactory(factory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        ObjectMapper mapper = objectMapper();
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(mapper);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    // ChatRoomListResponse 전용 RedisTemplate
    @Bean
    public RedisTemplate<String, ChatRoomListResponse> chatRoomRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ChatRoomListResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<ChatRoomListResponse> serializer = new Jackson2JsonRedisSerializer<>(ChatRoomListResponse.class);
        serializer.setObjectMapper(objectMapper());

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    // ChatMessageRedisEntity RedisTemplate
    @Bean
    public RedisTemplate<String, ChatMessageRedisEntity> messageRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, ChatMessageRedisEntity> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);

        // key 직렬화
        tpl.setKeySerializer(new StringRedisSerializer());
        // hash key 직렬화 (필요하다면)
        tpl.setHashKeySerializer(new StringRedisSerializer());

        // value 직렬화: DTO 타입 명시
        Jackson2JsonRedisSerializer<ChatMessageRedisEntity> ser =
                new Jackson2JsonRedisSerializer<>(ChatMessageRedisEntity.class);
        ser.setObjectMapper(objectMapper()); // LocalDateTime 처리용 mapper
        tpl.setValueSerializer(ser);
        tpl.setHashValueSerializer(ser);

        tpl.afterPropertiesSet();
        return tpl;
    }


    // Redis 리스너 컨테이너 - 두 개의 topic 리스너 등록
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapterMessage,
            MessageListenerAdapter listenerAdapterRoomList
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(listenerAdapterMessage, new PatternTopic("chatroom-message"));
        container.addMessageListener(listenerAdapterRoomList, new PatternTopic("chatroom-roomlist"));
        return container;
    }

    // 메시지 수신 어댑터 - sendMessage 메서드에 매핑
    @Bean
    public MessageListenerAdapter listenerAdapterMessage(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }

    // 메시지 수신 어댑터 - sendRoomList 메서드에 매핑
    @Bean
    public MessageListenerAdapter listenerAdapterRoomList(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendRoomList");
    }

    // 메시지 토픽
    @Bean
    public ChannelTopic messageTopic() {
        return new ChannelTopic("chatroom-message");
    }

    // 채팅방 리스트 토픽
    @Bean
    public ChannelTopic roomListTopic() {
        return new ChannelTopic("chatroom-roomlist");
    }
}
