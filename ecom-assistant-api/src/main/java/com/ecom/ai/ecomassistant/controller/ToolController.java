package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.ai.tool.ChatToolService;
import com.ecom.ai.ecomassistant.ai.tool.ToolInfo;
import com.ecom.ai.ecomassistant.common.annotation.CurrentUserId;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tool")
@Slf4j
public class ToolController {

    @Autowired
    private ChatToolService chatService;

    @Autowired
    private ApplicationContext applicationContext;

    @PostMapping("/message")
    @RequiresPermissions({"system:dataset:*"})
    public ResponseEntity<String> sendMessage(
            @CurrentUserId String userId,
            @RequestBody String request) {

        try {
            String response = chatService.chat(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("用戶 {} 的聊天請求處理失敗: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("處理請求時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 取得用戶可用的工具清單
     */
    @GetMapping("/tools")
    @RequiresPermissions({"system:dataset:*"})
    public ResponseEntity<List<ToolInfo>> getAvailableTools(@CurrentUserId String userId) {
        try {
            List<ToolInfo> tools = chatService.getAvailableTools();
            log.info("用戶 {} 查詢可用工具，共 {} 個", userId, tools.size());
            return ResponseEntity.ok(tools);
        } catch (Exception e) {
            log.error("用戶 {} 取得工具清單失敗: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}