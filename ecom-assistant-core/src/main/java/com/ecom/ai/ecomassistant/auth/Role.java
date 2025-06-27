package com.ecom.ai.ecomassistant.auth;

import com.ecom.ai.ecomassistant.auth.permission.SystemPermission;

import java.util.Set;
import java.util.stream.Collectors;

public interface Role {
    String getDisplayName();
    String getDescription();
    Set<String> getPermissionCodes();

    default Set<String> permissionToCodes(Set<SystemPermission> permissions) {
        return permissions.stream()
                .map(SystemPermission::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }
}