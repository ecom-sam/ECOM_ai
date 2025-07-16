package com.ecom.ai.ecomassistant.ai.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicToolService {

    private final ApplicationContext applicationContext;

    private final PermissionService permissionService;

    private final Map<String, Object> toolBeans = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    // 延遲初始化，第一次調用時才執行
    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    initializeTools();
                    initialized = true;
                }
            }
        }
    }

    private void initializeTools() {
        log.info("開始初始化工具註冊表...");
        scanAndRegisterTools();
        log.info("工具註冊表初始化完成，共註冊 {} 個工具 Bean", toolBeans.size());
    }

    private void scanAndRegisterTools() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);
                Class<?> beanClass = bean.getClass();

                if (beanClass.getName().contains("$$")) {
                    beanClass = beanClass.getSuperclass();
                }

                // 檢查這個 Bean 是否有 @Tool 方法
                Method[] methods = beanClass.getDeclaredMethods();
                boolean hasToolMethods = false;
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Tool.class)) {
                        // 檢查權限
                        ToolPermission permission = method.getAnnotation(ToolPermission.class);
                        if (permissionService.hasToolPermission(permission)) {
                            hasToolMethods = true;
                            break;
                        }
                    }
                }

                if (hasToolMethods) {
                    toolBeans.put(beanName, bean);
                    log.debug("註冊工具 Bean: {}", beanName);
                }

            } catch (Exception e) {
                log.error("掃描 Bean {} 時發生錯誤", beanName, e);
            }
        }
    }

    /**
     * 取得有權限的工具 Bean 陣列（直接給 Spring AI 使用）
     */
    public Object[] getAvailableToolBeans() {
        ensureInitialized();
        return toolBeans.values().toArray();
    }

    /**
     * 取得工具資訊（供前端顯示）
     */
    public List<ToolInfo> getAvailableToolsInfo() {
        ensureInitialized();

        List<ToolInfo> toolInfos = new ArrayList<>();

        for (Object bean : toolBeans.values()) {
            Class<?> beanClass = bean.getClass();
            if (beanClass.getName().contains("$$")) {
                beanClass = beanClass.getSuperclass();
            }

            Method[] methods = beanClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Tool.class)) {
                    ToolPermission permission = method.getAnnotation(ToolPermission.class);

                    if (permissionService.hasToolPermission(permission)) {
                        Tool toolAnnotation = method.getAnnotation(Tool.class);

                        ToolInfo toolInfo = ToolInfo.builder()
                                .name(method.getName())
                                .description(getToolDescription(toolAnnotation))
                                .roles(permission != null ? Set.of(permission.roles()) : Set.of())
                                .permissions(permission != null ? Set.of(permission.permissions()) : Set.of())
                                .build();

                        toolInfos.add(toolInfo);
                    }
                }
            }
        }

        return toolInfos;
    }

    private String getToolDescription(Tool toolAnnotation) {
        return Optional.ofNullable(toolAnnotation)
                .map(Tool::description)
                .orElse("無描述");
    }
}