package com.ecom.ai.ecomassistant.core.dto.response;

import com.ecom.ai.ecomassistant.common.UserStatus;

import java.util.Set;

public record UserPermissionDto(
        String id,
        String name,
        String email,
        UserStatus status,
        Set<String> permissions
) { }

