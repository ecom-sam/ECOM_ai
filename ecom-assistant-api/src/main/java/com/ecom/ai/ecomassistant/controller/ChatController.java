package com.ecom.ai.ecomassistant.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/ai")
    public String generate(@RequestBody Map<String, String> body) {
        String userInput = body.get("userInput");

        return chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }
}
