package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.db.model.ChatContentRequest;
import com.ecom.ai.ecomassistant.db.model.ChatMessage;
import com.ecom.ai.ecomassistant.db.model.ChatRequest;
import com.ecom.ai.ecomassistant.db.repository.ChatMessageRepository;
import com.ecom.ai.ecomassistant.db.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMessageService chatMessageService;
    private final ChatMemory chatMemory;

    @Autowired
    private final ChatMessageRepository chatMessageRepository;

    @PostMapping("/ai/{username}/{topicId}")
    public String generate(@PathVariable String username, @PathVariable String topicId, @RequestBody ChatRequest request) {
        String chatCollection = "Chat";
        String userInput = request.getUserInput();

        List<ChatContentRequest> contentsList = chatMessageRepository.findRecentContents(username, topicId);
        Collections.reverse(contentsList);

        for (ChatContentRequest dto : contentsList) {
            List<ChatMessage.ContentItem> items = dto.getContents();

            UserMessage userMessage = new UserMessage(items.get(0).getMessage());
            AssistantMessage assistantMessage = new AssistantMessage(items.get(1).getMessage());

            chatMemory.add(topicId, userMessage);
            chatMemory.add(topicId, assistantMessage);
        }

        List<Message> messages = chatMemory.get(topicId);
        if (messages.size() > 10) {
            List<Message> lastMessages = messages.subList(messages.size() - 10, messages.size());
            chatMemory.clear(topicId);
            chatMemory.add(topicId, lastMessages);
        }

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

