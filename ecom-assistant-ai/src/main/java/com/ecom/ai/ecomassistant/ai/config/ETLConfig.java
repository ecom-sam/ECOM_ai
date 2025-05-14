package com.ecom.ai.ecomassistant.ai.config;

import com.ecom.ai.ecomassistant.ai.etl.FileProcessingRule;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ETLConfig {

    @Bean
    public SummaryMetadataEnricher summaryMetadata(OpenAiChatModel aiClient) {
        String prompt =
        """
        請幫我總結一下以下內容:
        {context_str}
        
        請盡量控制在100字內。
        """;
        return new SummaryMetadataEnricher(aiClient, List.of(
                SummaryMetadataEnricher.SummaryType.PREVIOUS,
                SummaryMetadataEnricher.SummaryType.NEXT),
                prompt,
                MetadataMode.ALL
        );
    }

    @Bean
    public FileProcessingRule defaultFileProcessingRule(FileProcessingRuleConfig ruleConfig) {
        return FileProcessingRule.builder()
                .name("default")
                .reader(ruleConfig.getReader())
                .transformers(ruleConfig.getTransformers())
                .build();
    }
}
