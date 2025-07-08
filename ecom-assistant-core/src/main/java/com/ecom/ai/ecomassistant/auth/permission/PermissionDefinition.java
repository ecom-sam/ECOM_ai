package com.ecom.ai.ecomassistant.auth.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PermissionDefinition {
    private String code;
    private String label;
    private String description;

    public PermissionDefinition(Permission permission) {
        this.code = permission.getCode();
        this.label = permission.getLabel();
        this.description = permission.getDescription();
    }
}
