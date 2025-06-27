package com.ecom.ai.ecomassistant.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserInviteRequest(
        @Schema( description = "email", example = "user@example.com")
        @NotBlank
        String email
) {
}
