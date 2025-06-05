package com.ecom.ai.ecomassistant.core.chat.memory.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.content.Media;

import java.util.List;
import java.util.Map;

public abstract class AssistantMessageMixin {

    @JsonCreator
    public AssistantMessageMixin(
            @JsonProperty("text") String content,
            @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("toolCalls") List<AssistantMessage.ToolCall> toolCalls,
            @JsonProperty("media") List<Media> media
    ) {
    }
}
