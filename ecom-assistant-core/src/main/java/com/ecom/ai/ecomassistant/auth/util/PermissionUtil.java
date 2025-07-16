package com.ecom.ai.ecomassistant.auth.util;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;

import java.util.Set;

public class PermissionUtil {

    /**
     * 檢查是否有任一權限（OR），否則丟出例外
     */
    public static void checkAnyPermission(String permission) {
        checkAnyPermission(Set.of(permission));
    }

    public static void checkAnyPermission(Set<String> permissions) {
        if (!hasAnyPermission(permissions)) {
            throw new UnauthorizedException("缺少任一權限: " + String.join(", ", permissions));
        }
    }

    /**
     * 檢查是否有全部權限（AND），否則丟出例外
     */
    public static void checkAllPermission(Set<String> permissions) {
        if (!hasAllPermission(permissions)) {
            throw new UnauthorizedException("缺少必要權限: " + String.join(", ", permissions));
        }
    }

    /**
     * 回傳是否有任一權限（OR）
     */
    public static boolean hasAnyPermission(Set<String> permissions) {
        Subject subject = SecurityUtils.getSubject();
        for (String permission : permissions) {
            if (subject.isPermitted(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 回傳是否有全部權限（AND）
     */
    public static boolean hasAllPermission(Set<String> permissions) {
        Subject subject = SecurityUtils.getSubject();
        for (String permission : permissions) {
            if (!subject.isPermitted(permission)) {
                return false;
            }
        }
        return true;
    }

    // ===== 新增角色檢查方法 =====

    /**
     * 檢查是否有任一角色（OR），否則丟出例外
     */
    public static void checkAnyRole(String role) {
        checkAnyRole(Set.of(role));
    }

    public static void checkAnyRole(Set<String> roles) {
        if (!hasAnyRole(roles)) {
            throw new UnauthorizedException("缺少任一角色: " + String.join(", ", roles));
        }
    }

    /**
     * 檢查是否有全部角色（AND），否則丟出例外
     */
    public static void checkAllRole(Set<String> roles) {
        if (!hasAllRole(roles)) {
            throw new UnauthorizedException("缺少必要角色: " + String.join(", ", roles));
        }
    }

    /**
     * 回傳是否有任一角色（OR）
     */
    public static boolean hasAnyRole(Set<String> roles) {
        Subject subject = SecurityUtils.getSubject();
        for (String role : roles) {
            if (subject.hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 回傳是否有全部角色（AND）
     */
    public static boolean hasAllRole(Set<String> roles) {
        Subject subject = SecurityUtils.getSubject();
        for (String role : roles) {
            if (!subject.hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    // ===== 組合檢查方法 =====

    /**
     * 檢查角色和權限（同時滿足）
     */
    public static void checkRoleAndPermission(Set<String> roles, Set<String> permissions) {
        if (!hasAnyRole(roles) || !hasAnyPermission(permissions)) {
            throw new UnauthorizedException("缺少必要角色或權限 - 角色: " + String.join(", ", roles)
                    + " 權限: " + String.join(", ", permissions));
        }
    }

    /**
     * 檢查角色或權限（任一滿足即可）
     */
    public static boolean hasRoleOrPermission(Set<String> roles, Set<String> permissions) {
        return hasAnyRole(roles) || hasAnyPermission(permissions);
    }

    /**
     * 拋出權限拒絕例外
     */
    public static void forbidden() {
        throw new UnauthorizedException("缺少必要權限");
    }

    /**
     * 取得當前使用者的所有角色
     */
    public static Set<String> getCurrentUserRoles() {
        // 注意：Shiro 本身沒有直接取得所有角色的方法
        // 這需要根據你的 Realm 實作來調整
        Subject subject = SecurityUtils.getSubject();
        // 這裡需要自訂實作，可能需要從 Session 或其他地方取得
        return Set.of(); // 暫時返回空集合
    }
}