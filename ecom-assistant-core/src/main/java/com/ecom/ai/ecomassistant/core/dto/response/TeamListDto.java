package com.ecom.ai.ecomassistant.core.dto.response;

public record TeamListDto(
        String id,
        String name,
        String description,
        boolean isOwner,
        boolean isMember,
        Integer userCount
) {
}
