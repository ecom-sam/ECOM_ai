package com.ecom.ai.ecomassistant.controller;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.ecom.ai.ecomassistant.db.model.ChatMessage;
import com.ecom.ai.ecomassistant.db.model.ChatRequest;
import com.ecom.ai.ecomassistant.db.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;

import com.ecom.ai.ecomassistant.db.config.CouchbaseConfig;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.Instant;

import static org.springframework.ai.chat.memory.ChatMemory.DEFAULT_CONVERSATION_ID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMessageService chatMessageService;
    private final ChatMemory chatMemory;
    private final Cluster couchbaseCluster;
    private final CouchbaseConfig couchbaseConfig;

    @PostMapping("/ai/{username}")
    public String generate(@PathVariable String username, @RequestBody ChatRequest request) {

        String userInput = request.getUserInput();
        boolean resetHistory = request.isResetHistory();

        if (!resetHistory){
            String query = String.format("""
                SELECT c.contents
                FROM `%s`.`%s`.`%s` c
                WHERE c.username = $username
                ORDER BY c.datetime DESC
                LIMIT 4
            """, couchbaseConfig.getBucketName(), couchbaseConfig.getScopeName(), "Chat");

            QueryOptions options = QueryOptions.queryOptions()
                    .parameters(JsonObject.create().put("username", username));

            QueryResult result = couchbaseCluster.query(query, options);

            List<JsonObject> rows = result.rowsAsObject();
            Collections.reverse(rows); 

            for (JsonObject row : rows) {
                JsonArray contents = row.getArray("contents");

                JsonObject userContent = contents.getObject(0);
                JsonObject assistantContent = contents.getObject(1);

                UserMessage userMessage = new UserMessage(userContent.getString("message"));
                AssistantMessage assistantMessage = new AssistantMessage(assistantContent.getString("message"));

                chatMemory.add(DEFAULT_CONVERSATION_ID, userMessage);
                chatMemory.add(DEFAULT_CONVERSATION_ID, assistantMessage);
            }
        }

        List<ChatMessage.ContentItem> contents = new ArrayList<>();
        Instant datetime = Instant.now();
        String id = username + "." + datetime.toString();

        ChatMessage userMessage = new ChatMessage();
        userMessage.setId(id);
        userMessage.setUsername(username);
        userMessage.setDatetime(datetime);

        ChatMessage.ContentItem content = new ChatMessage.ContentItem("user", userInput);
        contents.add(content);

        String response = chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
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

