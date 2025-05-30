package com.ecom.ai.ecomassistant.db.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.core.mapping.id.IdAttribute;
import org.springframework.data.couchbase.repository.Collection;

import java.time.Instant;

@Data
@Builder
@Document
@Collection("chat-record")
public class ChatRecord {

    @Id
    @GeneratedValue
    private String id;

    @IdAttribute
    private String chatRecordId;
    private String topicId;
    private String userId;
    private String role;
    private String content;
    private Instant datetime;
    private String replyTo;


}
