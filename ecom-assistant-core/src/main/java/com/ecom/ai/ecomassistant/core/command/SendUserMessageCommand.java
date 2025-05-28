package com.ecom.ai.ecomassistant.core.command;

public record SendUserMessageCommand(
        String topicId,
        String userId,
        String userMessage
) {
}