 package com.ecom.ai.ecomassistant.ai.service;

import com.ecom.ai.ecomassistant.resource.file.FileInfo;
import com.ecom.ai.ecomassistant.ai.etl.FileProcessingRule;
import com.ecom.ai.ecomassistant.ai.etl.ProcessingRuleResolver;
import com.ecom.ai.ecomassistant.ai.etl.reader.EcomDocumentReader;
import com.ecom.ai.ecomassistant.ai.etl.transformer.EcomDocumentTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EtlService {

    private final Map<String, EcomDocumentReader> readerMap;
    private final Map<String, EcomDocumentTransformer> transformerMap;
    private final ProcessingRuleResolver ruleResolver;
    private final VectorStore vectorStore;

    public List<Document> processFile(FileInfo fileInfo) {
        List<Document> documents;

        FileProcessingRule processingRule = ruleResolver.resolve(fileInfo);
        var reader = readerMap.get(processingRule.getReader());
        documents = reader.read(fileInfo);

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
