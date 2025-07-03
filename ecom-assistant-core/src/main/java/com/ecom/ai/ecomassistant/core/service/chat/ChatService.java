package com.ecom.ai.ecomassistant.core.service.chat;

import com.ecom.ai.ecomassistant.core.command.SendUserMessageCommand;
import com.ecom.ai.ecomassistant.db.model.ChatRecord;
import com.ecom.ai.ecomassistant.db.service.ChatRecordService;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRecordService chatRecordService;
    private final ChatClient chatClient;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;

    public List<ChatRecord> findRecordsByTopicBefore(String topicId, String chatRecordId, Integer inputLimit) {
        int limit = inputLimit != null ? inputLimit : 10;
        return chatRecordService.findByTopicBefore(topicId, chatRecordId, limit);
    }

    public Flux<String> performAiChatFlow(SendUserMessageCommand command) {

        ChatRecord chatRecord = addUserMessage(command);

        var requestSpec = chatClient.prompt()
                .user(command.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, command.topicId()));

        List<String> datasetIds = command.datasetIds();
        if (command.withRag() == Boolean.TRUE && !CollectionUtils.isEmpty(datasetIds)) {
            String datasetIdString = datasetIds.stream()
                    .map(s -> String.format("'%s'", s))
                    .collect(Collectors.joining(", "));
            requestSpec
                    .advisors(retrievalAugmentationAdvisor)
                    .advisors(a -> a.param(
                            VectorStoreDocumentRetriever.FILTER_EXPRESSION,
                            String.format("datasetId IN [%s]", datasetIdString)
                    ));
        }

        List<String> responseBuffer = new ArrayList<>();
        Flux<String> aiStream = requestSpec
                .stream()
                .chatClientResponse()
                .switchOnFirst((this::processFirstResponse))
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

    protected Flux<String> processFirstResponse(
            Signal<? extends ChatClientResponse> signal,
            Flux<ChatClientResponse> flux
    ) {
        // 提取 context 並發送
        String contextIds = getAndSetChatContext(signal);

        // 建立包含 context 的 Flux
        Flux<String> contextFlux = contextIds != null ?
                Flux.just("context:" + contextIds) : Flux.empty();

        // 處理 AI 回應的 Flux
        Flux<String> responseFlux = flux
                .mapNotNull(r -> Optional.ofNullable(r.chatResponse())
                        .map(ChatResponse::getResult)
                        .map(Generation::getOutput)
                        .map(AssistantMessage::getText)
                        .orElse(null))
                .filter(StringUtils::hasLength);

        return Flux.concat(contextFlux, responseFlux);
    }

    protected String getAndSetChatContext(
            Signal<? extends ChatClientResponse> signal
    ) {
        return Optional.ofNullable(signal.hasValue() ? signal.get() : null)
                .map(ChatClientResponse::context)
                .map(ctx -> ctx.get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT))
                .filter(obj -> obj instanceof List<?>)
                .map(obj -> {
                    List<?> list = (List<?>) obj;
                    return list.stream()
                            .filter(item -> item instanceof Document)
                            .map(item -> ((Document) item).getId())
                            .collect(Collectors.toList());
                })
                .map(ids -> String.join(",", ids))
                .orElse(null);
    }
}
