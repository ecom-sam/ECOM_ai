package com.ecom.ai.ecomassistant.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record TeamRoleUpdateRequest(
        @NotBlank
        String name,

        @NotNull
        String description,

        @NotEmpty
        Set<String> permissions
) {
}
