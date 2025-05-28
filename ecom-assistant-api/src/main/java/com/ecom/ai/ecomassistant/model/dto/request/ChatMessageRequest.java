package com.ecom.ai.ecomassistant.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank String message
) {}
