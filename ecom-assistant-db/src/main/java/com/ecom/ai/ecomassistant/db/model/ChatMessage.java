package com.ecom.ai.ecomassistant.db.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.repository.Collection;

import java.time.Instant;

@Data
@Collection("Chat")
public class ChatMessage {
    @Id
    //@GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    private String id;

    @NotNull(message = "Username cannot be null")
    private String username;

    @NotNull(message = "Role cannot be null")
    private String role;

    @NotNull(message = "Message cannot be null")
    private String message;

    @NotNull(message = "Datetime cannot be null")
    private Instant datetime;
}
