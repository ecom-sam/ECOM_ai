package com.ecom.ai.ecomassistant.core.dto.command;

import java.util.Set;

public record TeamRoleUpdateCommand(
        String teamId,
        String name,
        String description,
        Set<String> permissions
) {
}
