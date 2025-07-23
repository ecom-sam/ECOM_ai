package com.ecom.ai.ecomassistant.core.dto.response;

public record TeamInviteCandidateDto (
        String id,
        String name,
        String email,
        boolean isMember
) {}