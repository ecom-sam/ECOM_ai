package com.ecom.ai.ecomassistant.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @Schema( description = "使用者顯示名稱", example = "willy")
        @NotBlank
        String name
) {
}
