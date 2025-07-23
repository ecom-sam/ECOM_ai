package com.ecom.ai.ecomassistant.core.dto.command;

import java.util.Set;

public record TeamMembersInviteCommand(
        String teamId,
        Set<String> userIds,
        Set<String> roleIds,
        Set<String> inviteEmails
) {
}
