package com.ecom.ai.ecomassistant.db.model.dto;

import java.util.Set;

public record TeamRoleDto(
        String id,
        String name,
        String description,
        Set<String> permissions,
        Boolean isSystemRole,
        Integer userCount
) {
}
