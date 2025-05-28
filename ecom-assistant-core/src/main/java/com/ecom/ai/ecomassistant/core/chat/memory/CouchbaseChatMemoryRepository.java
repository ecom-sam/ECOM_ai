package com.ecom.ai.ecomassistant.core.chat.memory;

import com.ecom.ai.ecomassistant.db.model.ChatContentRequest;
import com.ecom.ai.ecomassistant.db.model.ChatMessage;
import com.ecom.ai.ecomassistant.db.repository.ChatMessageRepository;
import com.ecom.ai.ecomassistant.db.repository.ChatRecordRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Primary
@Component
@RequiredArgsConstructor
public class CouchbaseChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMessageRepository chatMessageRepository;

    private final ChatRecordRepository chatRecordRepository;

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


    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return chatRecordRepository.findAllByTopicId(conversationId)
                .stream()
                .flatMap(chatRecord -> {
                    Message userMessage = new UserMessage(
                            chatRecord.getUserMessage() != null
                                    ? chatRecord.getUserMessage().getContent()
                                    : Strings.EMPTY
                    );
                    Message assistantMessage = new AssistantMessage(
                            chatRecord.getAiMessage() != null
                                    ? chatRecord.getAiMessage().getContent()
                                    : Strings.EMPTY
                    );
                    return Stream.of(userMessage, assistantMessage);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        //empty
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        //empty
    }
}
