package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.ai.repository.CouchbaseChatMemoryRepository;
import com.ecom.ai.ecomassistant.db.model.ChatMessage;
import com.ecom.ai.ecomassistant.db.model.ChatRequest;
import com.ecom.ai.ecomassistant.db.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatClient chatClient;
    private final ChatMessageService chatMessageService;
    private final ChatMemory chatMemory;
    private final CouchbaseChatMemoryRepository chatMemoryRepository;

    @PostMapping("/ai/{username}/{topicId}")
    public String generate(@PathVariable String username, @PathVariable String topicId, @RequestBody ChatRequest request) {
        String userInput = request.getUserInput();

        chatMemoryRepository.loadRecentMessagesIntoMemory(chatMemory, username, topicId);

        List<ChatMessage.ContentItem> contents = new ArrayList<>();
        Instant datetime = Instant.now();
        String id = username + "." + topicId + "." + datetime.toString();

        ChatMessage userMessage = new ChatMessage();
        userMessage.setId(id);
        userMessage.setUsername(username);
        userMessage.setTopicId(topicId);
        userMessage.setDatetime(datetime);

        ChatMessage.ContentItem content = new ChatMessage.ContentItem("user", userInput);
        contents.add(content);

        String response = chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(topicId).build())
                .user(userInput)
                .call()
                .content();

        content = new ChatMessage.ContentItem("assistant", response);
        contents.add(content);
        userMessage.setContents(contents);

        chatMessageService.createMessage(userMessage);

        return response;
    }
}

