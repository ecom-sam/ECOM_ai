package com.ecom.ai.ecomassistant.ai.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ChatToolService {

    @Autowired
    private DynamicToolService toolService;

    @Autowired
    ChatClient.Builder builder;

    @Autowired
    private PermissionService permissionService;

    public String chat(String userInput) {
        // 取得有權限的工具 Bean
        Object[] availableTools = toolService.getAvailableToolBeans();

        log.info("為用戶載入了 {} 個可用工具", availableTools.length);

        // 調用 ChatClient - 直接傳遞工具 Bean
        String response = builder.build().prompt()
                .user(userInput)
                .tools(availableTools)  // 傳遞工具 Bean 而不是 Function
                .call()
                .content();

        return response;
    }

    /**
     * 取得用戶可用工具清單
     */
    public List<ToolInfo> getAvailableTools() {
        return toolService.getAvailableToolsInfo();
    }

    /**
     * 檢查用戶是否有特定工具的權限
     */
    public boolean canUseTool(String toolName) {
        return toolService.getAvailableToolsInfo().stream()
                .anyMatch(tool -> tool.getName().equals(toolName));
    }
}
