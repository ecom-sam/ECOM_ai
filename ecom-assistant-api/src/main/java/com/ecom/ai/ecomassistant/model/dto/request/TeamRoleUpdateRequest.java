package com.ecom.ai.ecomassistant.model.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record TeamRoleUpdateRequest(
        String name,

        String description,

        @NotEmpty
        Set<String> permissions
) {
}
