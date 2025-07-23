package com.ecom.ai.ecomassistant.ai.tool;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

// 工具資訊 DTO
@Data
@Builder
public class ToolInfo {
    private String name;
    private String description;
    private Set<String> roles;
    private Set<String> permissions;
}
