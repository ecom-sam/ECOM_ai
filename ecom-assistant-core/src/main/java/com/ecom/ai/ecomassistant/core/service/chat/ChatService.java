package com.ecom.ai.ecomassistant.core.service.chat;

import com.ecom.ai.ecomassistant.core.command.SendUserMessageCommand;
import com.ecom.ai.ecomassistant.db.model.ChatRecord;
import com.ecom.ai.ecomassistant.db.model.ChatTopic;
import com.ecom.ai.ecomassistant.db.service.ChatRecordService;
import com.ecom.ai.ecomassistant.db.service.ChatTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatTopicService chatTopicService;
    private final ChatRecordService chatRecordService;
    private final ChatClient chatClient;

    public ChatTopic createChatTopic(String topic, String userId) {
        ChatTopic chatTopic = new ChatTopic();
        chatTopic.setTopic(topic);
        chatTopic.setUserId(userId);
        chatTopic.setCreateDateTime(Instant.now());
        return chatTopicService.save(chatTopic);
    }

    public List<ChatTopic> findAllChatTopicsByUser(String userId) {
        return chatTopicService.findAllByUserId(userId);
    }

    public Flux<String> performAiChatFlow(SendUserMessageCommand command) {

        ChatRecord chatRecord = addUserMessage(command);

        List<String> responseBuffer = new ArrayList<>();
        Flux<String> aiStream = chatClient.prompt()
                .user(command.userMessage())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, command.topicId()))
                .stream()
                .content()
                .doOnNext(responseBuffer::add)
                .doOnComplete(() -> {
                    String aiMessage = String.join("", responseBuffer);
                    addAiMessage(chatRecord, aiMessage);
                });

        return Flux.concat(
                Flux.just("chatId:" + chatRecord.getId()),
                aiStream
        );
    }

    protected ChatRecord addUserMessage(SendUserMessageCommand command) {
        ChatRecord chatRecord = ChatRecord.builder()
                .topicId(command.topicId())
                .userId(command.userId())
                .userMessage(ChatRecord.ChatMessage.fromUser(command.userMessage()))
                .build();
        return chatRecordService.save(chatRecord);
    }

    protected void addAiMessage(ChatRecord chatRecord, String aiMessage) {
        chatRecord.setAiMessage(ChatRecord.ChatMessage.fromAi(aiMessage));
        chatRecordService.save(chatRecord);
    }
}
