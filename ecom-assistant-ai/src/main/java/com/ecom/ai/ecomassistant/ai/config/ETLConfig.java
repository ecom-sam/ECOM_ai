package com.ecom.ai.ecomassistant.ai.config;

import com.ecom.ai.ecomassistant.ai.etl.FileProcessingRule;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ETLConfig {

    @Bean
    public SummaryMetadataEnricher summaryMetadata(OpenAiChatModel aiClient) {
        return new SummaryMetadataEnricher(aiClient, List.of(
                SummaryMetadataEnricher.SummaryType.PREVIOUS,
                SummaryMetadataEnricher.SummaryType.CURRENT,
                SummaryMetadataEnricher.SummaryType.NEXT)
        );
    }

    @Bean
    public FileProcessingRule defaultFileProcessingRule() {
        return FileProcessingRule.builder()
                .name("default")
                .reader("defaultPagePdfDocumentReader")
                .transformers(List.of(
                        "defaultTokenTextSplitter"
                        //"defaultSummaryMetadataEnricher"
                ))
                .build();
    }
}
