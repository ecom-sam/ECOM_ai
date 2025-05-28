package com.ecom.ai.ecomassistant.db.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.repository.Collection;

import java.time.Instant;

@Data
@Builder
@Document
@Collection("chat-record")
public class ChatRecord {

    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    private String id;
    private String topicId;
    private String userId;
    private ChatMessage userMessage;
    private ChatMessage aiMessage;

    @Getter
    @Builder
    public static class ChatMessage {

        private String role;
        private String content;
        private Instant datetime;

        public static ChatMessage fromUser(String content) {
            return ChatMessage.builder()
                    .role("user")
                    .content(content)
                    .datetime(Instant.now())
                    .build();
        }

        public static ChatMessage fromAi(String content) {
            return ChatMessage.builder()
                    .role("assistant")
                    .content(content)
                    .datetime(Instant.now())
                    .build();
        }
    }
}
