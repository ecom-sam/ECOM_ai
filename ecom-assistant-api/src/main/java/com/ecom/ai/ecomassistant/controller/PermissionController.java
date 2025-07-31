package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.auth.permission.PermissionDefinition;
import com.ecom.ai.ecomassistant.auth.permission.PermissionRegistry;
import com.ecom.ai.ecomassistant.core.service.UserManager;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import com.ecom.ai.ecomassistant.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
    
    private final UserService userService;
    private final UserManager userManager;


    @GetMapping("/system")
    @RequiresPermissions({"system:*"})
    public List<PermissionDefinition> getSystemPermissions() {
        return PermissionRegistry.getSystemPermissions();
    }

    @GetMapping("/team")
    public PermissionRegistry.TeamPermissionGroup getTeamPermissions() {
        Subject subject = SecurityUtils.getSubject();
        
        // 檢查是否有任一權限（OR 邏輯）
        boolean hasSystemPermission = subject.isPermitted("system:*") || 
                                    subject.isPermitted("system:team:*");
        
        if (hasSystemPermission) {
            return PermissionRegistry.getTeamLevelPermissions();
        }
        
        // 檢查是否有任何團隊的角色管理權限
        // 獲取用戶的完整權限列表來檢查
        String jwt = (String) subject.getPrincipal();
        String userId = JwtUtil.getUserId(jwt);
        User user = userService.findById(userId).orElseThrow();
        UserManager.UserRoleContext context = userManager.getUserRoleContext(user);
        
        // 檢查是否有任何團隊的 roles:manage 權限
        // 這允許團隊角色管理員存取權限定義列表，以便管理自己團隊的角色
        // 包括：team:{teamId}:roles:manage 或 team:{teamId}:* (team owner)
        boolean hasTeamRoleManagePermission = context.permissions().stream()
                .anyMatch(perm -> perm.contains(":roles:manage") || 
                                  (perm.startsWith("team:") && perm.endsWith(":*")));
        
        if (!hasTeamRoleManagePermission) {
            throw new UnauthorizedException("需要系統管理權限或團隊角色管理權限");
        }
        
        return PermissionRegistry.getTeamLevelPermissions();
    }
}
