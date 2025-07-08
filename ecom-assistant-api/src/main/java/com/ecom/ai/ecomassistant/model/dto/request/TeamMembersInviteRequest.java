package com.ecom.ai.ecomassistant.model.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record TeamMembersInviteRequest(
        @Nullable
        Set<String> userIds,

        @NotEmpty
        Set<String> roleIds,

        @Nullable
        Set<String> inviteEmails
) {
}
