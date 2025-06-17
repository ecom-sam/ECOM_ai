package com.ecom.ai.ecomassistant.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum Permission {

    SYSTEM_SUPER_ADMIN("system:*", "system"),

    SYSTEM_TEAM_ADMIN("system:team:*", "system"),
    SYSTEM_TEAM_VIEW("system:team:view", "system"),
    SYSTEM_TEAM_MANAGE("system:team:manage", "system"),
    SYSTEM_TEAM_GRANT_ADMIN("system:team:grant-admin", "system"),

    SYSTEM_USER_ADMIN("system:user:*", "system"),
    SYSTEM_USER_LIST("system:user:list", "system"),
    SYSTEM_USER_VIEW("system:user:view", "system"),
    SYSTEM_USER_INVITE("system:user:invite", "system"),
    SYSTEM_USER_MANAGE("system:user:manage", "system");

    private final String code;
    private final String group;

    public static Optional<Permission> fromCode(String code) {
        return Arrays.stream(values())
                .filter(p -> p.code.equals(code))
                .findFirst();
    }

    public static List<Permission> fromGroup(String group) {
        return Arrays.stream(values())
                .filter(p -> p.group.equalsIgnoreCase(group))
                .collect(Collectors.toList());
    }

    public static Optional<Permission> fromName(String name) {
        try {
            return Optional.of(Permission.valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
