package com.ecom.ai.ecomassistant.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "etl.file-processing.default")
public class FileProcessingRuleConfig {

    private String reader = "defaultPagePdfDocumentReader";
    private List<String> transformers = List.of(
            "defaultTokenTextSplitter",
            "defaultPdfImageExtractor",
            "defaultImageEnricher"
    );
}
