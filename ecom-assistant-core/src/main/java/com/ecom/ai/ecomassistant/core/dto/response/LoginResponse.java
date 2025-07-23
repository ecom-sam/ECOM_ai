package com.ecom.ai.ecomassistant.core.dto.response;

public record LoginResponse(
        String token,
        UserPermissionDto user
) {
}
