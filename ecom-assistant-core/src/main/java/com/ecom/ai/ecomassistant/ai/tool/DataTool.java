package com.ecom.ai.ecomassistant.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class DataTool {

    @Tool(description = "取得姓名")
    @ToolPermission(tags = {"datetime", "readonly"}, roles = {"system:SUPER_ADMIN"})
    public String getName() {
        return "willy-123";
    }
}
