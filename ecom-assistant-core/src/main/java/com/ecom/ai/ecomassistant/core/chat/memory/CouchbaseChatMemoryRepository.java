package com.ecom.ai.ecomassistant.core.chat.memory;

import com.couchbase.client.java.Cluster;
import com.ecom.ai.ecomassistant.core.chat.memory.mixin.AssistantMessageMixin;
import com.ecom.ai.ecomassistant.core.chat.memory.mixin.MessageMixin;
import com.ecom.ai.ecomassistant.core.chat.memory.mixin.UserMessageMixin;
import com.ecom.ai.ecomassistant.db.config.CouchbaseCacheProperties;
import com.ecom.ai.ecomassistant.db.model.ChatContentRequest;
import com.ecom.ai.ecomassistant.db.model.ChatMessage;
import com.ecom.ai.ecomassistant.db.repository.ChatMessageRepository;
import com.ecom.ai.ecomassistant.db.repository.ChatRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Primary
@Component
@RequiredArgsConstructor
public class CouchbaseChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRecordRepository chatRecordRepository;
    private final CouchbaseCacheProperties cacheProperties;
    private final CouchbaseTemplate couchbaseTemplate;
    private final CacheManager cacheManager;

    private Cache chatMemoryCache;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        chatMemoryCache = cacheManager.getCache("chat-memory");
        assert chatMemoryCache != null;

        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(Message.class, MessageMixin.class);
        objectMapper.addMixIn(UserMessage.class, UserMessageMixin.class);
        objectMapper.addMixIn(AssistantMessage.class, AssistantMessageMixin.class);
    }


    @Override
    public List<String> findConversationIds() {
        Cluster cluster = couchbaseTemplate.getCouchbaseClientFactory().getCluster();
        String statement = String.format("SELECT META().id AS id FROM `%s`.`%s`.`%s`",
                couchbaseTemplate.getBucketName(),
                cacheProperties.getScopeName(),
                cacheProperties.getCollectionName()
        );

        return cluster.query(statement)
                .rowsAsObject().stream()
                .map(row -> row.getString("id"))
                .collect(Collectors.toList());
    }

    @Override
    //@Cacheable(cacheNames = "chatMemory", key = "#conversationId")
    public List<Message> findByConversationId(String conversationId) {
        var cachedMessages = Optional.ofNullable(chatMemoryCache.get(conversationId))
                .map(Cache.ValueWrapper::get)
                .orElse(null);

        //return cached data
        if (cachedMessages instanceof List<?> m) {
            return m.stream()
                    .map(item -> objectMapper.convertValue(item, Message.class))
                    .collect(Collectors.toList());
        }

        //find in database
        List<Message> storedMessages = findByConversationIdInternal(conversationId);
        if (CollectionUtils.isEmpty(storedMessages)) {
            return List.of();
        } else {
            storedMessages.removeLast();
            chatMemoryCache.put(conversationId, storedMessages);
            return storedMessages;
        }
    }

    private List<Message> findByConversationIdInternal(String conversationId) {
        return chatRecordRepository.findLatestChatByTopicId(conversationId)
                .stream()
                .map(chatRecord -> {
                    String content = chatRecord.getContent();
                    String role = chatRecord.getRole();
                    return switch (role) {
                        case "USER" -> new UserMessage(content);
                        case "ASSISTANT" -> new AssistantMessage(content);
                        default -> throw new RuntimeException();
                    };
                })
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        chatMemoryCache.put(conversationId, messages);
    }


    @Override
    public void deleteByConversationId(String conversationId) {
        chatMemoryCache.evict(conversationId);
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
