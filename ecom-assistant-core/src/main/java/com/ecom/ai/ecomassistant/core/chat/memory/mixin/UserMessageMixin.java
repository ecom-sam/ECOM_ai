package com.ecom.ai.ecomassistant.core.chat.memory.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.content.Media;

import java.util.Collection;
import java.util.Map;

public abstract class UserMessageMixin {

    @JsonCreator
    public UserMessageMixin(
            @JsonProperty("text") String textContent,
            @JsonProperty("media") Collection<Media> media,
            @JsonProperty("metadata") Map<String, Object> metadata
    ) {}
}
