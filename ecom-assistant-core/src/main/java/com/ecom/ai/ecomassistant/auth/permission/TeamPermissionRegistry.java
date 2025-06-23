package com.ecom.ai.ecomassistant.auth.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeamPermissionRegistry {

    public static List<PermissionDefinition> getAllPermissions() {
        List<PermissionDefinition> result = new ArrayList<>();

        for (Permission p : TeamPermission.values()) {
            result.add(new PermissionDefinition(p.getCode(), p.getGroup(), p.getLabel()));
        }
        for (Permission p : DatasetPermission.values()) {
            result.add(new PermissionDefinition(p.getCode(), p.getGroup(), p.getLabel()));
        }

        return result;
    }

    public static Map<String, List<PermissionDefinition>> getGroupedPermissions() {
        return getAllPermissions().stream()
                .collect(Collectors.groupingBy(PermissionDefinition::getGroup));
    }
}