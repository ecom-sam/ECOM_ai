package com.ecom.ai.ecomassistant.auth.permission;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum TeamPermission implements Permission {

    TEAM_VIEW("team:view", "team", "檢視團隊資訊"),
    TEAM_EDIT("team:edit", "team", "編輯團隊"),
    TEAM_INVITE_MEMBERS("team:invite-members", "team", "邀請團隊成員"),
    TEAM_MANAGE_MEMBERS("team:manage-members", "team", "管理團隊成員"),
    TEAM_DELETE("team:delete", "team", "刪除團隊");

    private final String code;
    private final String group;
    private final String label;

    public static Optional<TeamPermission> fromCode(String code) {
        return Permission.fromCode(TeamPermission.class, code);
    }

    public static List<TeamPermission> fromGroup(String group) {
        return Permission.fromGroup(TeamPermission.class, group);
    }

    public static Optional<TeamPermission> fromName(String name) {
        return Permission.fromName(TeamPermission.class, name);
    }
}