package com.ecom.ai.ecomassistant.core.dto.response;

public record TeamListDto(
        String id,
        String name,
        boolean isOwner,
        boolean isMember,
        Integer userCount
) {
}
