package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.auth.permission.PermissionDefinition;
import com.ecom.ai.ecomassistant.auth.permission.PermissionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {


    @GetMapping("/system")
    public List<PermissionDefinition> getSystemPermissions() {
        return PermissionRegistry.getSystemPermissions();
    }

    @GetMapping("/team")
    public PermissionRegistry.TeamPermissionGroup getTeamPermissions() {
        return PermissionRegistry.getTeamLevelPermissions();
    }
}
