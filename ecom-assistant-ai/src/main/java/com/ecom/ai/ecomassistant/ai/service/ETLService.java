 package com.ecom.ai.ecomassistant.ai.service;

import com.ecom.ai.ecomassistant.ai.etl.FileProcessingRule;
import com.ecom.ai.ecomassistant.ai.etl.ProcessingRuleResolver;
import com.ecom.ai.ecomassistant.ai.etl.reader.EcomDocumentReader;
import com.ecom.ai.ecomassistant.ai.etl.transformer.EcomDocumentTransformer;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ETLService {

    private static final Logger log = LoggerFactory.getLogger(ETLService.class);
    private final Map<String, EcomDocumentReader> readerMap;
    private final Map<String, EcomDocumentTransformer> transformerMap;
    private final ProcessingRuleResolver ruleResolver;
    private final VectorStore vectorStore;

    public List<Document> processFile(String path) {
        List<Document> documents;

        FileProcessingRule processingRule = ruleResolver.resolve(new File(path));
        var reader = readerMap.get(processingRule.getReader());
        documents = reader.read(path);

        for (String transformerKey : processingRule.getTransformers()) {
            var transformer = transformerMap.get(transformerKey);
            if (transformer == null) {
                log.warn("Transformer with key '{}' not found. Skipping.", transformerKey);
                continue;
            }
            documents = transformer.transform(documents);
        }

        return documents;
    }

    public void save(List<Document> documents) {
        vectorStore.add(documents);
    }
}
