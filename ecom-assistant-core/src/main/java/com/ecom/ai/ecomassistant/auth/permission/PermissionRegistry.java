package com.ecom.ai.ecomassistant.auth.permission;

import java.util.Arrays;
import java.util.List;

public class PermissionRegistry {

    public static List<PermissionDefinition> getSystemPermissions() {
        return Arrays.stream(SystemPermission.values())
                .map(PermissionDefinition::new)
                .toList();
    }

    public record TeamPermissionGroup(
            List<PermissionDefinition> team,
            List<PermissionDefinition> dataset
    ) {}

    public static TeamPermissionGroup getTeamLevelPermissions() {
        return new TeamPermissionGroup(
                Arrays.stream(TeamPermission.values())
                        .map(PermissionDefinition::new)
                        .toList(),
                Arrays.stream(DatasetPermission.values())
                        .map(PermissionDefinition::new)
                        .toList()
        );
    }
}
