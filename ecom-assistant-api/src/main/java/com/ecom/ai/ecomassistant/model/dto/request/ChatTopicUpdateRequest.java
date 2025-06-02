package com.ecom.ai.ecomassistant.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatTopicUpdateRequest {
    @NotBlank
    private String topic;
}
