package com.ecom.ai.ecomassistant.db.model;

import lombok.Data;

import java.util.List;

@Data
public class ChatContentRequest {
    private List<ChatMessage.ContentItem> contents;
}
