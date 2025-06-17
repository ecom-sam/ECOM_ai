package com.ecom.ai.ecomassistant.core.dto.command;

public record TeamCreateCommand(
        String name,
        String description,
        String adminEmail
) {
}
