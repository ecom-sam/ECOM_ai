package com.ecom.ai.ecomassistant.auth;

import com.ecom.ai.ecomassistant.auth.permission.SystemPermission;
import lombok.Getter;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum SystemRole {

    SUPER_ADMIN("system super admin", "", Set.of(
            SystemPermission.SYSTEM_SUPER_ADMIN)
    ),

    TEAM_ADMIN("system team admin", "",  Set.of(
            SystemPermission.SYSTEM_TEAM_ADMIN,
            SystemPermission.SYSTEM_USER_LIST
    )),
    TEAM_VIEWER("system team viewer", "",  Set.of(
            SystemPermission.SYSTEM_TEAM_VIEW
    )),

    USER_ADMIN("system user admin", "", Set.of(
            SystemPermission.SYSTEM_USER_ADMIN
    )),
    USER_MANAGER("system user manager", "", Set.of(
            SystemPermission.SYSTEM_USER_MANAGE,
            SystemPermission.SYSTEM_USER_VIEW,
            SystemPermission.SYSTEM_USER_INVITE
    )),
    USER("user", "", Set.of());

    private final String displayName;
    private final String description;
    private final Set<String> permissionCodes;

    SystemRole(String name, String description, Set<SystemPermission> permissions) {
        this.displayName = name;
        this.description = description;
        this.permissionCodes = permissions.stream()
                .map(SystemPermission::getCode)
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
