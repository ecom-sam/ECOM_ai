package com.ecom.ai.ecomassistant.ai.tool;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Set;

@Data
@Builder
public class ToolMetadata {
    private String name;
    private String description;
    private Object bean;
    private Method method;
    private ToolPermission toolPermission;
    private Set<String> roles;
    private Set<String> permissions;
}