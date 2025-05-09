package com.ecom.ai.ecomassistant.model;

import lombok.Builder;
import lombok.Getter;

@Builder
public record Role(
        String group,
        RoleType role
) {
    @Getter
    public enum RoleType {
        GROUP_ADMIN("Group admin"),
        User("User");

        private final String displayName;

        RoleType(String displayName) {
            this.displayName = displayName;
        }

    }
}
