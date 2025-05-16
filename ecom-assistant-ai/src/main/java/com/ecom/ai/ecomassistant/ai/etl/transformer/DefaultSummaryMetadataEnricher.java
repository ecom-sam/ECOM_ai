package com.ecom.ai.ecomassistant.ai.etl.transformer;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultSummaryMetadataEnricher implements EcomDocumentTransformer {

    private final SummaryMetadataEnricher enricher;

    @Override
    public List<Document> transform(List<Document> documents) {
        return this.enricher.apply(documents);
    }
}
