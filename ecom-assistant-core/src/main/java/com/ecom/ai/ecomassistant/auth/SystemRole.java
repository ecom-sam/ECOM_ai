package com.ecom.ai.ecomassistant.auth;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum SystemRole {

    ROLE_SUPER_ADMIN("system super admin", "", Set.of(
            Permission.SYSTEM_SUPER_ADMIN)
    ),

    ROLE_TEAM_ADMIN("system team admin", "",  Set.of(
            Permission.SYSTEM_TEAM_ADMIN)
    ),
    ROLE_TEAM_MANAGER("system team manager", "",  Set.of(
            Permission.SYSTEM_TEAM_MANAGE,
            Permission.SYSTEM_TEAM_VIEW
    )),
    ROLE_TEAM_VIEWER("system team viewer", "",  Set.of(
            Permission.SYSTEM_TEAM_VIEW
    )),

    ROLE_USER_ADMIN("system user admin", "", Set.of(
            Permission.SYSTEM_USER_ADMIN)
    ),
    ROLE_USER_MANAGER("system user manager", "", Set.of(
            Permission.SYSTEM_USER_MANAGE)
    ),
    ROLE_USER("user", "", Set.of());

    private final String displayName;
    private final String description;
    private final Set<String> permissionCodes;

    SystemRole(String name, String description, Set<Permission> permissions) {
        this.displayName = name;
        this.description = description;
        this.permissionCodes = permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Optional<SystemRole> fromName(String name) {
        try {
            return Optional.of(SystemRole.valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
