package com.ecom.ai.ecomassistant.core.service.chat;

import com.ecom.ai.ecomassistant.core.command.SendUserMessageCommand;
import com.ecom.ai.ecomassistant.db.model.ChatRecord;
import com.ecom.ai.ecomassistant.db.model.ChatTopic;
import com.ecom.ai.ecomassistant.db.service.ChatRecordService;
import com.ecom.ai.ecomassistant.db.service.ChatTopicService;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.MessageType;
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
    private final QuestionAnswerAdvisor questionAnswerAdvisor;

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

        var requestSpec = chatClient.prompt()
                .user(command.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, command.topicId()));

        if (command.withRag() == Boolean.TRUE) {
            requestSpec.advisors(questionAnswerAdvisor);
        }

        List<String> responseBuffer = new ArrayList<>();
        Flux<String> aiStream = requestSpec
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
                .chatRecordId(UlidCreator.getUlid().toString())
                .topicId(command.topicId())
                .userId(command.userId())
                .role(MessageType.USER.name())
                .content(command.message())
                .datetime(Instant.now())
                .build();
        return chatRecordService.save(chatRecord);
    }

    protected void addAiMessage(ChatRecord chatRecord, String aiMessage) {
        ChatRecord aiChatRecord = ChatRecord.builder()
                .chatRecordId(UlidCreator.getUlid().toString())
                .topicId(chatRecord.getTopicId())
                .userId(chatRecord.getUserId())
                .role(MessageType.ASSISTANT.name())
                .content(aiMessage)
                .replyTo(chatRecord.getId())
                .datetime(Instant.now())
                .build();
        chatRecordService.save(aiChatRecord);
    }
}
