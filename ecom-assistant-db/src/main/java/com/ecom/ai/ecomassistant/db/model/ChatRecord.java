package com.ecom.ai.ecomassistant.db.model;

import lombok.Builder;
import lombok.Data;
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
    private Message userMessage;
    private Message aiMessage;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
        private Instant datetime;

        public static Message userMessage(String content) {
            return Message.builder()
                    .role("user")
                    .content(content)
                    .datetime(Instant.now())
                    .build();
        }

        public static Message aiMessage(String content) {
            return Message.builder()
                    .role("ai")
                    .content(content)
                    .datetime(Instant.now())
                    .build();
        }
    }
}
