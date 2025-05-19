package com.ecom.ai.ecomassistant.chat;

public record ChatRecord(
        String id,
        String chatId,
        String userId,
        Long timestamp,
        String messageType,
        String message
) {
}
