 package com.ecom.ai.ecomassistant.ai.service;

import com.ecom.ai.ecomassistant.ai.etl.FileProcessingRule;
import com.ecom.ai.ecomassistant.ai.etl.ProcessingRuleResolver;
import com.ecom.ai.ecomassistant.ai.etl.reader.EcomDocumentReader;
import com.ecom.ai.ecomassistant.ai.etl.transformer.EcomDocumentTransformer;
import com.ecom.ai.ecomassistant.common.resource.file.FileInfo;
import com.ecom.ai.ecomassistant.db.model.QAPair;
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
    private final QAGenerationService qaGenerationService;

    public List<Document> processFile(FileInfo fileInfo) {
        List<Document> documents;

        FileProcessingRule processingRule = ruleResolver.resolve(fileInfo);
        var reader = readerMap.get(processingRule.getReader());
        documents = reader.read(fileInfo);

        EcomDocumentTransformer.transform(documents, fileInfo);

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

    public List<QAPair> processFileWithQA(FileInfo fileInfo, String datasetId, String datasetName, String documentId) {
        try {
            log.info("Processing file with Q/A generation: {} (dataset: {})", fileInfo.fileName(), datasetName);
            
            // Process documents using existing pipeline
            List<Document> documents = processFile(fileInfo);
            
            // Add datasetId to document metadata for RAG filtering
            documents.forEach(doc -> {
                doc.getMetadata().put("datasetId", datasetId);
                doc.getMetadata().put("datasetName", datasetName);
                doc.getMetadata().put("documentId", documentId);
            });
            
            // Save to vector store
            save(documents);
            
            // Generate and save Q/A pairs
            List<QAPair> qaPairs = qaGenerationService.generateAndSaveQAPairs(
                    documents, 
                    datasetId, 
                    datasetName, 
                    fileInfo.fileName(), 
                    fileInfo.fileName(), 
                    documentId
            );
            
            log.info("Successfully processed file {} with {} documents and {} Q/A pairs", 
                    fileInfo.fileName(), documents.size(), qaPairs.size());
            
            return qaPairs;
            
        } catch (Exception e) {
            log.error("Error processing file with Q/A generation {}: {}", fileInfo.fileName(), e.getMessage(), e);
            throw e;
        }
    }
}
