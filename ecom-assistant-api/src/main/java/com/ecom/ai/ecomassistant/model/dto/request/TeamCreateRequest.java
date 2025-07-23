package com.ecom.ai.ecomassistant.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TeamCreateRequest(
        @NotBlank
        String name,

        @NotBlank
        String ownerEmail
) {
}
