package com.ecom.ai.ecomassistant.auth.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionRegistry {

    public static List<PermissionDefinition> getSystemPermissions() {
        List<PermissionDefinition> result = new ArrayList<>();

        for (Permission p : SystemPermission.values()) {
            result.add(new PermissionDefinition(p.getCode(), p.getLabel()));
        }

        return result;
    }

    public record TeamPermissionGroup(
            List<PermissionDefinition> team,
            List<PermissionDefinition> dataset
    ) {}

    public static TeamPermissionGroup getTeamLevelPermissions() {
        return new TeamPermissionGroup(
                Arrays.stream(TeamPermission.values())
                        .map(p -> new PermissionDefinition(p.getCode(), p.getLabel()))
                        .toList(),
                Arrays.stream(DatasetPermission.values())
                        .map(p -> new PermissionDefinition(p.getCode(), p.getLabel()))
                        .toList()
        );
    }
}
