package com.ecom.ai.ecomassistant.ai.repository;

import com.ecom.ai.ecomassistant.db.model.ChatContentRequest;
import com.ecom.ai.ecomassistant.db.model.ChatMessage;
import com.ecom.ai.ecomassistant.db.repository.ChatMessageRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class CouchbaseChatMemoryRepository  {

    private final ChatMessageRepository chatMessageRepository;

    public CouchbaseChatMemoryRepository(ChatMessageRepository repo) {
        this.chatMessageRepository = repo;
    }

    public void loadRecentMessagesIntoMemory(ChatMemory chatMemory, String username, String topicId) {
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
            List<Message> trimmed = messages.subList(messages.size() - 10, messages.size());
            chatMemory.clear(topicId);
            chatMemory.add(topicId, trimmed);
        }
    }
}
