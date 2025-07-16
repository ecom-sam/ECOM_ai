package com.ecom.ai.ecomassistant.ai.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolPermission {
    String[] tags() default {};
    String[] roles() default {};
    String[] permissions() default {}; // 新增權限檢查

    // 邏輯操作類型
    LogicType roleLogic() default LogicType.OR;     // 角色邏輯：OR 或 AND
    LogicType permissionLogic() default LogicType.OR; // 權限邏輯：OR 或 AND

    enum LogicType {
        OR, AND
    }
}