package com.ecom.ai.ecomassistant.model.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record TeamMemberRoleUpdateRequest(
        @NotEmpty
        Set<String> roles
) {
}
