package com.ecom.ai.ecomassistant.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AiConfig {

    @Bean
    ChatClient helperChatClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(ChatOptions
                        .builder()
                        .temperature(0.0)
                        .model("gpt-4.1-mini")
                        .build())
                .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ChatMemoryRepository chatMemoryRepository) {

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(6)
                .build();

        return builder
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore).build();
    }

    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {

        chatClientBuilder
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.0)
                        .build());

        return RetrievalAugmentationAdvisor.builder()
//                .queryTransformers(RewriteQueryTransformer.builder()
//                        .chatClientBuilder(chatClientBuilder)
//                        .build())
//                .queryExpander(MultiQueryExpander.builder()
//                        .chatClientBuilder(chatClientBuilder)
//                        .numberOfQueries(3)
//                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.30)
                        .topK(6)
                        .vectorStore(vectorStore)
                        .build())
                .build();
    }
}


