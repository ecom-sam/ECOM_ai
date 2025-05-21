package com.ecom.ai.ecomassistant.db.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.repository.Collection;

import java.time.Instant;
import java.util.List;

@Data
@Collection("Chat")
public class ChatMessage {
    @Id
    //@GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    private String id;

    @NotNull(message = "Username cannot be null")
    private String username;

    @NotNull(message = "Contents cannot be null")
    @Valid
    private List<ContentItem> contents;

    @NotNull(message = "Datetime cannot be null")
    private Instant datetime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentItem {
        @NotNull(message = "Role cannot be null")
        private String role;

        @NotNull(message = "Message cannot be null")
        private String message;
    }
}






