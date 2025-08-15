package com.bwabwayo.app.domain.chat.service;

import com.bwabwayo.app.domain.chat.domain.ChatMessageMongoEntity;
import com.bwabwayo.app.domain.chat.dto.MessageDTO;
import com.bwabwayo.app.domain.chat.repository.ChatMessageRepository;
import com.bwabwayo.app.global.common.CommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatMongoService {

    private final ChatMessageRepository chatMessageRepository;
    private final MongoTemplate mongoTemplate;
    private final CommonService commonService;

    // 채팅 저장
    @Transactional
    public MessageDTO save(MessageDTO chatMessageDto) {

        ChatMessageMongoEntity chatMessage = chatMessageRepository.save(ChatMessageMongoEntity.of(chatMessageDto, commonService.parseSafe(chatMessageDto.getCreatedAt())));
        log.info("save success : {}", chatMessage.getContent());
        return MessageDTO.fromEntity(chatMessage);
    }

    // 채팅 불러오기
    @Transactional(readOnly = true)
    public List<MessageDTO> findAll(Long roomId, Integer pageNumber) {
        Page<ChatMessageMongoEntity> page = findByRoomIdWithPaging(roomId, pageNumber, 20);
        return page.getContent().stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
    }



    private Page<ChatMessageMongoEntity> findByRoomIdWithPaging(Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")); // 정방향 정렬로 변경

        Query query = new Query()
                .addCriteria(Criteria.where("roomId").is(roomId))
                .with(pageable);

        List<ChatMessageMongoEntity> result = mongoTemplate.find(query, ChatMessageMongoEntity.class, "chat_messages");

        // count 쿼리는 skip/limit 제거된 상태로 실행해야 정확함
        Query countQuery = new Query(Criteria.where("roomId").is(roomId));

        return PageableExecutionUtils.getPage(
                result,
                pageable,
                () -> mongoTemplate.count(countQuery, ChatMessageMongoEntity.class, "chat_messages")
        );
    }

    public ChatMessageMongoEntity findLatestMessageByRoomId(String roomId) {
        try {
            Query query = new Query(Criteria.where("roomId").is(roomId))
                    .with(Sort.by(Sort.Order.desc("_id")))
                    .limit(1);

            return mongoTemplate.findOne(query, ChatMessageMongoEntity.class);
        } catch (Exception e) {
            return null;
        }
    }



}
