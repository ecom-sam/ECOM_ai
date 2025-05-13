package com.ecom.ai.ecomassistant.ai.etl.transformer;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DefaultDatasetInfoEnricher implements EcomDocumentTransformer {

    @Override
    public List<Document> transform(List<Document> documents) {
        for (var document : documents) {
            var metadata = document.getMetadata();
            metadata.putAll(Map.of(
                    "datasetId", "dataset-123",
                    "documentId", "doc-123")
            );
        }
        return documents;
    }
}
