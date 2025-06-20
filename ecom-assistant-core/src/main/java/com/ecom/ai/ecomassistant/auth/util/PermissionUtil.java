package com.ecom.ai.ecomassistant.auth.util;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;

import java.util.Set;

public class PermissionUtil {

    /**
     * 檢查是否有任一權限（OR）
     */
    public static void checkAnyPermission(String permission) {
        checkAnyPermission(Set.of(permission));
    }

    public static void checkAnyPermission(Set<String> permissions) {
        Subject subject = SecurityUtils.getSubject();
        for (String permission : permissions) {
            if (subject.isPermitted(permission)) {
                return;
            }
        }
        throw new UnauthorizedException("缺少任一權限: " + String.join(", ", permissions));
    }

    /**
     * 檢查是否有全部權限（AND）
     */
    public static void checkAllPermission(Set<String> permissions) {
        Subject subject = SecurityUtils.getSubject();
        for (String permission : permissions) {
            if (!subject.isPermitted(permission)) {
                throw new UnauthorizedException("缺少必要權限: " + permission);
            }
        }
    }

}
