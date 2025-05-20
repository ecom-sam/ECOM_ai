package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.db.model.ChatMessage;
import com.ecom.ai.ecomassistant.db.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMessageService chatMessageService;
    private final ChatMemory chatMemory;

    @PostMapping("/ai/{username}")
    public String generate(@PathVariable String username, @RequestBody Map<String, String> body) {
        String userInput = body.get("userInput");
        Instant datetime = Instant.now();
        String id = username + "." + datetime.toString();

        ChatMessage userMessage = new ChatMessage();
        userMessage.setId(id);
        userMessage.setUsername(username);
        userMessage.setRole("user");
        userMessage.setMessage(userInput);
        userMessage.setDatetime(datetime);
        chatMessageService.createMessage(userMessage);

        String response = chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .user(userInput)
                .call()
                .content();

        datetime = Instant.now();
        id = "AI" + "." + username + "." + datetime.toString();

        ChatMessage AIMessage = new ChatMessage();
        AIMessage.setId(id);
        AIMessage.setUsername("AI" + "." + username);
        AIMessage.setRole("assistant");
        AIMessage.setMessage(response);
        AIMessage.setDatetime(datetime);
        chatMessageService.createMessage(AIMessage);

        return response;
    }
}

