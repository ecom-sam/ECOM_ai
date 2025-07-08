package com.ecom.ai.ecomassistant.auth.permission;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum TeamPermission implements Permission {

    TEAM_VIEW("team:view", "team", "檢視團隊資訊", "檢視團隊資訊"),
    TEAM_EDIT("team:edit", "team", "編輯團隊", "編輯團隊"),
    //TEAM_DELETE("team:delete", "team", "刪除團隊"),

    TEAM_MEMBERS_VIEW("members:view", "team", "查看團隊成員", "查看團隊成員"),
    TEAM_MEMBERS_INVITE("members:invite", "team", "邀請團隊成員", "邀請團隊成員"),
    TEAM_MEMBERS_MANAGE("members:manage", "team", "管理團隊成員", "管理團隊成員"),

    TEAM_ROLES_VIEW("roles:view", "team", "查看團隊角色", "查看團隊角色"),
    TEAM_ROLES_MANAGE("roles:manage", "team", "管理團隊角色", "管理團隊角色");

    private final String code;
    private final String group;
    private final String label;
    private final String description;

    public static Optional<TeamPermission> fromCode(String code) {
        return Permission.fromCode(TeamPermission.class, code);
    }

    public static List<TeamPermission> fromGroup(String group) {
        return Permission.fromGroup(TeamPermission.class, group);
    }

    public static Optional<TeamPermission> fromName(String name) {
        return Permission.fromName(TeamPermission.class, name);
    }

    public String getCodeWithTeamId(String teamId) {
        return String.format("team:%s:%s", teamId, code);
    }
}