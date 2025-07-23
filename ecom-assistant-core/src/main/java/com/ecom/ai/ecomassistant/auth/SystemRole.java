package com.ecom.ai.ecomassistant.auth;

import com.ecom.ai.ecomassistant.auth.permission.SystemPermission;
import lombok.Getter;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum SystemRole implements Role {

    SUPER_ADMIN("system super admin", "", Set.of(
            SystemPermission.SYSTEM_SUPER_ADMIN)
    ),
    USER("user", "", Set.of()),

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

    DATASET_ADMIN("system dataset admin", "", Set.of(
            SystemPermission.SYSTEM_DATASET_ADMIN
    ));



    private final String displayName;
    private final String description;
    private final Set<String> permissionCodes;

    SystemRole(String name, String description, Set<SystemPermission> permissions) {
        this.displayName = name;
        this.description = description;
        this.permissionCodes = permissionToCodes(permissions);
    }

    public static Optional<SystemRole> fromName(String name) {
        try {
            return Optional.of(SystemRole.valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
