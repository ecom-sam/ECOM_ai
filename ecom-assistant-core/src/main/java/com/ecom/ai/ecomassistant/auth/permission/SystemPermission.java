package com.ecom.ai.ecomassistant.auth.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum SystemPermission implements Permission {

    SYSTEM_SUPER_ADMIN("system:*", "system", "系統超級管理權限"),

    SYSTEM_TEAM_ADMIN("system:team:*", "system", "團隊管理全部權限"),
    SYSTEM_TEAM_VIEW("system:team:view", "system", "檢視團隊"),
    SYSTEM_TEAM_MANAGE("system:team:manage", "system", "管理團隊"),
    SYSTEM_TEAM_GRANT_ADMIN("system:team:grant-admin", "system", "授權團隊管理員"),

    SYSTEM_USER_ADMIN("system:user:*", "system", "使用者全部權限"),
    SYSTEM_USER_LIST("system:user:list", "system", "使用者清單"),
    SYSTEM_USER_VIEW("system:user:view", "system", "檢視使用者"),
    SYSTEM_USER_INVITE("system:user:invite", "system", "邀請使用者"),
    SYSTEM_USER_MANAGE("system:user:manage", "system", "管理使用者");

    private final String code;
    private final String group;
    private final String label;

    public static Optional<SystemPermission> fromCode(String code) {
        return Permission.fromCode(SystemPermission.class, code);
    }

    public static List<SystemPermission> fromGroup(String group) {
        return Permission.fromGroup(SystemPermission.class, group);
    }

    public static Optional<SystemPermission> fromName(String name) {
        return Permission.fromName(SystemPermission.class, name);
    }
}
