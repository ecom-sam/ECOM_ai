package com.ecom.ai.ecomassistant.ai.etl.transformer;

import com.ecom.ai.ecomassistant.common.context.DatasetContext;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DefaultDatasetInfoEnricher implements EcomDocumentTransformer {

    @Override
    public List<Document> transform(List<Document> documents) {
        var contextData = DatasetContext.getDatasetContextData();
        for (var document : documents) {
            var metadata = document.getMetadata();
            metadata.putAll(Map.of(
                    "datasetId", contextData.datasetId(),
                    "documentId", contextData.documentId())
            );
        }
        return documents;
    }
}
