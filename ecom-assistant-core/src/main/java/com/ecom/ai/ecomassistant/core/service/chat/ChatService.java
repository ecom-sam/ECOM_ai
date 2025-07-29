package com.ecom.ai.ecomassistant.core.service.chat;

import com.ecom.ai.ecomassistant.ai.service.TieredRAGService;
import com.ecom.ai.ecomassistant.core.dto.command.SendUserMessageCommand;
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
    private final TieredRAGService tieredRAGService;

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
            log.info("Using Tiered RAG with datasets: {}", datasetIds);
            
            // 使用兩層優先級 RAG 檢索（QA-vector 優先，再搜尋 document-vector）
            List<Document> ragDocuments = tieredRAGService.retrieveWithDatasetFilter(
                    command.message(), datasetIds);
            
            log.info("Tiered RAG retrieved {} documents for query", ragDocuments.size());
            
            // 手動添加檢索到的文檔作為上下文
            if (!ragDocuments.isEmpty()) {
                StringBuilder contextBuilder = new StringBuilder();
                contextBuilder.append("以下是相關的參考資料，請根據這些資料回答問題：\n\n");
                
                for (int i = 0; i < ragDocuments.size(); i++) {
                    Document doc = ragDocuments.get(i);
                    String tier = (String) doc.getMetadata().getOrDefault("searchTier", "unknown");
                    String priority = (String) doc.getMetadata().getOrDefault("priority", "normal");
                    
                    contextBuilder.append(String.format("參考資料 %d (%s - %s):\n", 
                            i + 1, tier, priority));
                    contextBuilder.append(doc.getFormattedContent());
                    contextBuilder.append("\n\n");
                }
                
                contextBuilder.append("請基於以上參考資料回答用戶的問題。如果參考資料中沒有相關信息，請誠實說明。\n\n");
                contextBuilder.append("用戶問題: ").append(command.message());
                
                requestSpec.user(contextBuilder.toString());
                
                // 為了保持與現有系統的兼容性，仍然添加 document context
                requestSpec.advisors(a -> a.param("DOCUMENT_CONTEXT", ragDocuments));
            }
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
                .map(ctx -> {
                    // 先嘗試從 RetrievalAugmentationAdvisor 獲取
                    Object documents = ctx.get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
                    if (documents == null) {
                        // 如果沒有，嘗試從我們自定義的 DOCUMENT_CONTEXT 獲取
                        documents = ctx.get("DOCUMENT_CONTEXT");
                    }
                    return documents;
                })
                .filter(obj -> obj instanceof List<?>)
                .map(obj -> {
                    List<?> list = (List<?>) obj;
                    return list.stream()
                            .filter(item -> item instanceof Document)
                            .map(item -> {
                                Document doc = (Document) item;
                                String tier = (String) doc.getMetadata().getOrDefault("searchTier", "");
                                String priority = (String) doc.getMetadata().getOrDefault("priority", "");
                                // 在 context 中包含檢索層級信息
                                return doc.getId() + (tier.isEmpty() ? "" : ":" + tier + ":" + priority);
                            })
                            .collect(Collectors.toList());
                })
                .map(ids -> String.join(",", ids))
                .orElse(null);
    }
}
