package com.ecom.ai.ecomassistant.ai.tool;

import com.ecom.ai.ecomassistant.auth.util.PermissionUtil;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionService {

    /**
     * 檢查工具權限（使用 Shiro）
     */
    public boolean hasToolPermission(ToolPermission toolPermission) {
        if (toolPermission == null) {
            return true; // 沒有權限註解，預設允許
        }

        // 檢查角色
        if (toolPermission.roles().length > 0) {
            if (!hasRequiredRoles(toolPermission.roles(), toolPermission.roleLogic())) {
                return false;
            }
        }

        // 檢查權限
        if (toolPermission.permissions().length > 0) {
            if (!hasRequiredPermissions(toolPermission.permissions(), toolPermission.permissionLogic())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 檢查是否具備所需角色
     */
    private boolean hasRequiredRoles(String[] roles, ToolPermission.LogicType logic) {
        Set<String> roleSet = Set.of(roles);

        if (logic == ToolPermission.LogicType.AND) {
            return PermissionUtil.hasAllRole(roleSet);
        } else {
            return PermissionUtil.hasAnyRole(roleSet);
        }
    }

    /**
     * 檢查是否具備所需權限
     */
    private boolean hasRequiredPermissions(String[] permissions, ToolPermission.LogicType logic) {
        Set<String> permissionSet = Set.of(permissions);

        if (logic == ToolPermission.LogicType.AND) {
            return PermissionUtil.hasAllPermission(permissionSet);
        } else {
            return PermissionUtil.hasAnyPermission(permissionSet);
        }
    }

}