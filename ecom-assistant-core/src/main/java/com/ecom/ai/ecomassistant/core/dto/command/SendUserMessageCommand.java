package com.ecom.ai.ecomassistant.core.dto.command;

import java.util.List;

public record SendUserMessageCommand(
        String topicId,
        String userId,
        String message,
        Boolean withRag,
        List<String> datasetIds
) {
}