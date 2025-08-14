package com.bwabwayo.app.domain.chat.repository;

import com.bwabwayo.app.domain.chat.domain.ChatMessageMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageMongoEntity, Long> {
}
