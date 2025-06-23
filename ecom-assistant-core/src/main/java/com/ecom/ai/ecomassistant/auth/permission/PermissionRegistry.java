package com.ecom.ai.ecomassistant.auth.permission;

import java.util.ArrayList;
import java.util.List;

public class PermissionRegistry {

    public static List<PermissionDefinition> getSystemPermissions() {
        List<PermissionDefinition> result = new ArrayList<>();

        for (Permission p : SystemPermission.values()) {
            result.add(new PermissionDefinition(p.getCode(), p.getGroup(), p.getLabel()));
        }

        return result;
    }

    public static List<PermissionDefinition> getTeamLevelPermissions() {
        List<PermissionDefinition> result = new ArrayList<>();
        for (Permission p : TeamPermission.values()) {
            result.add(new PermissionDefinition(p.getCode(), p.getGroup(), p.getLabel()));
        }
        for (Permission p : DatasetPermission.values()) {
            result.add(new PermissionDefinition(p.getCode(), p.getGroup(), p.getLabel()));
        }
        return result;
    }
}
